package com.enterprise.ai.application;

import com.enterprise.ai.domain.TicketId;

/**
 * Unknown ticket identity [REQ-025]. HTTP mapping is an API concern.
 */
public final class TicketNotFoundException extends RuntimeException {

    private final TicketId ticketId;

    public TicketNotFoundException(TicketId ticketId) {
        super("Ticket was not found");
        this.ticketId = ticketId;
    }

    public TicketNotFoundException() {
        this(null);
    }

    public TicketId ticketId() {
        return ticketId;
    }
}
