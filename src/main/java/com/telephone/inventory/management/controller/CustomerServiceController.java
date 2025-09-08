package com.telephone.inventory.management.controller;

import com.common.models.exceptions.InvalidPhoneNumberException;
import com.common.models.model.ApiResponse;
import com.telephone.inventory.management.model.TransitionRequest;
import com.telephone.inventory.management.service.NumberBookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
public class CustomerServiceController {

    @Autowired
    private NumberBookingService bookingService;

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceController.class);

    @PostMapping("/numbers/{e164Number}/transition")
    public ResponseEntity<ApiResponse> applyTransition(@PathVariable("e164Number") String phoneNumber,
                                                       @RequestBody TransitionRequest request) {
        if (phoneNumber.isEmpty() || !phoneNumber.matches("\\+\\d{10,15}")) {
            String msg = String.format("Invalid record skipped: %s", phoneNumber);
            throw new InvalidPhoneNumberException(msg);
        }

        bookingService.transitionNumber(phoneNumber, request);

        log.info("Successfully updated number {}", phoneNumber);
        ApiResponse response = new ApiResponse("Successfully updated number " + phoneNumber, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
