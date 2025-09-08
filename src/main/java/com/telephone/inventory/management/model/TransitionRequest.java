package com.telephone.inventory.management.model;

import com.common.models.enums.PhoneNumberStatus;

public class TransitionRequest {
    private PhoneNumberStatus currentStatus;
    private PhoneNumberStatus nextStatus;
    private String correlationId;
    private String userId;

    public PhoneNumberStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(PhoneNumberStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public PhoneNumberStatus getNextStatus() {
        return nextStatus;
    }

    public void setNextStatus(PhoneNumberStatus nextStatus) {
        this.nextStatus = nextStatus;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}