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
        String requestBody = """
                    {
                        "e164Number": "+12345678901",
                        "currentStatus": "AVAILABLE",
                        "nextStatus": "RESERVED",
                        "userId": "user1"
                    }
                    """;

        mockMvc.perform(post("/customer/numbers/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(bookingService, times(1))
                .transitionNumber(eq("+12345678901"), any(TransitionRequest.class));
    }

    @Test
    void applyTransition_invalidNumber_returnsBadRequest() throws Exception {
        String requestBody = """
                    {
                        "e164Number": "123",
                        "currentStatus": "AVAILABLE",
                        "nextStatus": "RESERVED",
                        "userId": "user1"
                    }
                    """;

        mockMvc.perform(post("/customer/numbers/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(bookingService, times(0))
                .transitionNumber(anyString(), any(TransitionRequest.class));
    }

    @Test
    void applyTransition_emptyNumber_returnsBadRequest() throws Exception {
        String requestBody = """
                {
                    "e164Number": "",
                    "currentStatus": "AVAILABLE",
                    "nextStatus": "RESERVED",
                    "userId": "user1"
                }
                """;

        mockMvc.perform(post("/customer/numbers/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(bookingService, times(0))
                .transitionNumber(anyString(), any(TransitionRequest.class));
    }
}
