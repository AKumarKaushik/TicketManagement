package com.enterprise.ai.domain;

/**
 * Business rejection of a recognized but illegal status pair [REQ-016, REQ-028].
 * Not a validation error and not an HTTP status.
 */
public final class IllegalStatusTransitionException extends RuntimeException {

    public static final String ERROR_CODE = "ILLEGAL_TRANSITION";

    private final TicketStatus currentStatus;
    private final TicketStatus targetStatus;

    public IllegalStatusTransitionException(TicketStatus currentStatus, TicketStatus targetStatus) {
        super("Illegal status transition from " + currentStatus + " to " + targetStatus);
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public String errorCode() {
        return ERROR_CODE;
    }

    public TicketStatus currentStatus() {
        return currentStatus;
    }

    public TicketStatus targetStatus() {
        return targetStatus;
    }
}
