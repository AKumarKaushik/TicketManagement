package com.enterprise.ai.domain;

/**
 * Closed set of ticket statuses [REQ-014]. Unknown tokens are validation (VAL-005), not transitions.
 */
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED;

    public boolean isTerminal() {
        return this == CLOSED || this == CANCELLED;
    }

    /**
     * Parses an exact status token. Matching is case-sensitive and does not invent aliases.
     */
    public static TicketStatus fromToken(String token) {
        if (token == null || token.isBlank()) {
            throw new DomainValidationException(
                    "status",
                    "VAL-005",
                    "Status must be one of OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED");
        }
        try {
            return TicketStatus.valueOf(token);
        } catch (IllegalArgumentException ex) {
            throw new DomainValidationException(
                    "status",
                    "VAL-005",
                    "Status must be one of OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED");
        }
    }
}
