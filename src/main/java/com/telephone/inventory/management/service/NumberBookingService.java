package com.telephone.inventory.management.service;

import com.common.models.enums.PhoneNumberStatus;
import com.common.models.exceptions.NumberStatusUpdateException;
import com.common.models.exceptions.PhoneNumberDoesNotExistException;
import com.common.models.model.PhoneRecordPostgres;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.telephone.inventory.management.model.TransitionRequest;
import com.telephone.inventory.management.repository.PhoneRecordCassandraRepo;
import com.telephone.inventory.management.repository.PhoneRecordPostgresRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class NumberBookingService {

    private static final Logger log = LoggerFactory.getLogger(NumberBookingService.class);

    @Autowired
    private PhoneRecordCassandraRepo cassandraRepo;

    @Autowired
    private KafkaTemplate<Object, JsonNode> kafkaTemplate;

    @Autowired
    private PhoneRecordPostgresRepo postgresRepo;

    public void transitionNumber(String e164Number, TransitionRequest request) {
        PhoneNumberStatus current = request.getCurrentStatus();
        PhoneNumberStatus next = request.getNextStatus();

        if (!current.canTransitionTo(next)) {
            throw new IllegalStateException("Invalid transition: " + current + " → " + next);
        }

        Optional<PhoneRecordPostgres> optional = postgresRepo.findById(e164Number);

        if(optional.isEmpty()) {
            throw new PhoneNumberDoesNotExistException(String.format("Phone number %s does not exist", e164Number));
        }

        PhoneRecordPostgres obj = optional.get();

        if(!obj.getStatus().toValue().equals(current.toString())) {
            throw new IllegalStateException("Current status mismatch. Expected current status: " + current +
                    " but actual current status: " + obj.getStatus());
        }

        if(!current.equals(PhoneNumberStatus.AVAILABLE)) {
            if((request.getCorrelationId().isEmpty() || request.getUserId().isEmpty())) {
                String msg = String.format("Correlation id %s or User id %s is missing",
                        request.getCorrelationId(), request.getUserId());
                throw new IllegalStateException(msg);
            }

            if(!request.getUserId().equals(obj.getUserId())) {
                String msg = String.format("User id mismatch, expected value is %s but value in request is %s",
                        obj.getUserId(), request.getUserId());
                throw new IllegalStateException(msg);
            }
        } else {
            request.setCorrelationId(UUID.randomUUID().toString());
        }

        String userId = request.getUserId();
        String correlationId = request.getCorrelationId();

        // If telephone is available then userid must be null
        if(next.toValue().equals(PhoneNumberStatus.AVAILABLE.toValue())) {
            userId = null;
            correlationId = null;
        }

        int count = postgresRepo.updateIfMatch(correlationId, next, obj.getVersion()+1,
                userId, obj.getE164Number(), obj.getVersion(), current);

        if(count==0) {
            String msg = String.format(
                    "Number %d reservation failed: version mismatch or not AVAILABLE", obj.getVersion());
            throw new NumberStatusUpdateException(msg);
        }

        log.info("Number {} reserved successfully", obj.getE164Number());

        obj.setStatus(request.getNextStatus());
        obj.setVersion(obj.getVersion() + 1);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.valueToTree(obj);
        node.put("correlationId", request.getCorrelationId());
        node.put("userId", request.getUserId());
        JsonNode jsonNode = node;

        // DLQ logic has to be here in case of failure
        kafkaTemplate.send("post-processing", jsonNode);
    }
}
