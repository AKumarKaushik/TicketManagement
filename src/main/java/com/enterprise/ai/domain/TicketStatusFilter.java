package com.enterprise.ai.domain;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Status filter over tickets [REQ-010, REQ-023]. Equality is by the five approved status tokens.
 * Does not mutate tickets. Combined keyword+status is OQ-007 and is not provided here.
 */
public final class TicketStatusFilter {

    private TicketStatusFilter() {
    }

    public static List<Ticket> apply(Collection<Ticket> tickets, String statusToken) {
        return apply(tickets, TicketStatus.fromToken(statusToken));
    }

    public static List<Ticket> apply(Collection<Ticket> tickets, TicketStatus status) {
        Objects.requireNonNull(tickets, "tickets");
        if (status == null) {
            throw new DomainValidationException(
                    "status",
                    "VAL-005",
                    "Status must be one of OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED");
        }
        return tickets.stream().filter(ticket -> ticket.status() == status).toList();
    }
}
