package com.enterprise.ai.domain;

/**
 * Ticket identity. The application assigns a UUID string; the domain requires a
 * present non-blank value and does not generate identifiers.
 */
public record TicketId(String value) {

    public TicketId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Ticket identity must be present and not blank");
        }
    }
}
