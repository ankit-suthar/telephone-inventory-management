package com.telephone.inventory.management.service;

import com.common.models.enums.PhoneNumberStatus;
import com.common.models.exceptions.NumberStatusUpdateException;
import com.common.models.exceptions.PhoneNumberDoesNotExistException;
import com.common.models.model.PhoneRecordCassandra;
import com.telephone.inventory.management.model.TransitionRequest;
import com.telephone.inventory.management.repository.PhoneRecordCassandraRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NumberBookingServiceTest {

    @InjectMocks
    private NumberBookingService service;

    @Mock
    private PhoneRecordCassandraRepo cassandraRepo;

    @Mock
    private KafkaTemplate<Object, JsonNode> kafkaTemplate;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void transitionNumber_validAvailableToBooked_sendsKafkaMessage() {
        String e164 = "+12345678901";
        TransitionRequest request = new TransitionRequest();
        request.setCurrentStatus(PhoneNumberStatus.AVAILABLE);
        request.setNextStatus(PhoneNumberStatus.RESERVED);
        request.setUserId("user1");

        PhoneRecordCassandra record = new PhoneRecordCassandra();
        record.setE164Number(e164);
        record.setStatus(PhoneNumberStatus.AVAILABLE);
        record.setVersion(1);

        when(cassandraRepo.findById(e164)).thenReturn(Optional.of(record));
        when(cassandraRepo.updateIfMatch(anyString(), eq(PhoneNumberStatus.RESERVED),
                eq(2), eq(e164), eq(1), eq(PhoneNumberStatus.AVAILABLE)))
                .thenReturn(true);

        service.transitionNumber(e164, request);

        verify(cassandraRepo).updateIfMatch(anyString(), eq(PhoneNumberStatus.RESERVED),
                eq(2), eq(e164), eq(1), eq(PhoneNumberStatus.AVAILABLE));
        verify(kafkaTemplate).send(eq("post-processing"), any(ObjectNode.class));
    }

    @Test
    void transitionNumber_invalidTransition_throwsException() {
        String e164 = "+12345678901";
        TransitionRequest request = new TransitionRequest();
        request.setCurrentStatus(PhoneNumberStatus.ALLOCATED);
        request.setNextStatus(PhoneNumberStatus.AVAILABLE); // invalid backward transition

        assertThatThrownBy(() -> service.transitionNumber(e164, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid transition: " + request.getCurrentStatus()
                        + " → " + request.getNextStatus());
    }

    @Test
    void transitionNumber_numberDoesNotExist_throwsException() {
        String e164 = "+12345678901";
        TransitionRequest request = new TransitionRequest();
        request.setCurrentStatus(PhoneNumberStatus.AVAILABLE);
        request.setNextStatus(PhoneNumberStatus.RESERVED);

        when(cassandraRepo.findById(e164)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.transitionNumber(e164, request))
                .isInstanceOf(PhoneNumberDoesNotExistException.class)
                .hasMessageContaining(e164);
    }

    @Test
    void transitionNumber_statusMismatch_throwsException() {
        String e164 = "+12345678901";
        TransitionRequest request = new TransitionRequest();
        request.setCurrentStatus(PhoneNumberStatus.AVAILABLE);
        request.setNextStatus(PhoneNumberStatus.RESERVED);

        PhoneRecordCassandra record = new PhoneRecordCassandra();
        record.setE164Number(e164);
        record.setStatus(PhoneNumberStatus.RELEASED); // mismatch
        record.setVersion(1);

        when(cassandraRepo.findById(e164)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> service.transitionNumber(e164, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Current status mismatch");
    }

    @Test
    void transitionNumber_casFailure_throwsException() {
        String e164 = "+12345678901";
        TransitionRequest request = new TransitionRequest();
        request.setCurrentStatus(PhoneNumberStatus.AVAILABLE);
        request.setNextStatus(PhoneNumberStatus.RESERVED);
        request.setUserId("user1");

        PhoneRecordCassandra record = new PhoneRecordCassandra();
        record.setE164Number(e164);
        record.setStatus(PhoneNumberStatus.AVAILABLE);
        record.setVersion(1);

        when(cassandraRepo.findById(e164)).thenReturn(Optional.of(record));
        when(cassandraRepo.updateIfMatch(anyString(), eq(PhoneNumberStatus.RESERVED),
                eq(2), eq(e164), eq(1), eq(PhoneNumberStatus.AVAILABLE)))
                .thenReturn(false);

        assertThatThrownBy(() -> service.transitionNumber(e164, request))
                .isInstanceOf(NumberStatusUpdateException.class)
                .hasMessageContaining("reservation failed");
    }
}
