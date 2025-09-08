package com.telephone.inventory.management.controller;

import com.common.models.handler.GlobalExceptionHandler;
import com.telephone.inventory.management.model.TransitionRequest;
import com.telephone.inventory.management.service.NumberBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerServiceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NumberBookingService bookingService;

    @InjectMocks
    private CustomerServiceController controller;

    @BeforeEach
    void setup() {
        // Build MockMvc manually (standalone setup)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()) // optional: custom handler
                .build();
    }

    @Test
    void applyTransition_validNumber_callsService() throws Exception {
        String phoneNumber = "+12345678901"; // valid E164
        String requestBody = "{ \"status\": \"BOOKED\" }";

        mockMvc.perform(post("/customer/numbers/" + phoneNumber + "/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(bookingService, times(1))
                .transitionNumber(eq(phoneNumber), any(TransitionRequest.class));
    }

    @Test
    void applyTransition_invalidNumber_returnsBadRequest() throws Exception {
        String phoneNumber = "123"; // invalid E164
        String requestBody = "{ \"status\": \"BOOKED\" }";

        mockMvc.perform(post("/customer/numbers/" + phoneNumber + "/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(bookingService, times(0))
                .transitionNumber(anyString(), any(TransitionRequest.class));
    }

    @Test
    void applyTransition_emptyNumber_returnsBadRequest() throws Exception {
        String phoneNumber = ""; // empty
        String requestBody = "{ \"status\": \"BOOKED\" }";

        mockMvc.perform(post("/customer/numbers/" + phoneNumber + "/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));

        verify(bookingService, times(0))
                .transitionNumber(anyString(), any(TransitionRequest.class));
    }
}
