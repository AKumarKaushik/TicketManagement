package com.enterprise.ai.domain;

/**
 * Sole authority for allowed vs illegal status pairs [REQ-015, REQ-016, REQ-017].
 * Same-status requests are OQ-012 and are not classified here.
 */
public final class TicketLifecycle {

    private TicketLifecycle() {
    }

    /**
     * @return true for T1–T5; false for every status-changing pair in state-machine §4
     * @throws OpenQuestionDeferredException if {@code from} equals {@code to} (OQ-012)
     */
    public static boolean isAllowed(TicketStatus from, TicketStatus to) {
        if (from == null || to == null) {
            throw new DomainValidationException(
                    "status",
                    "VAL-005",
                    "Status must be one of OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED");
        }
        if (from == to) {
            throw new OpenQuestionDeferredException(
                    "OQ-012",
                    "Same-status change is deferred; not classified as allowed or illegal");
        }
        return switch (from) {
            case OPEN -> to == TicketStatus.IN_PROGRESS || to == TicketStatus.CANCELLED;
            case IN_PROGRESS -> to == TicketStatus.RESOLVED || to == TicketStatus.CANCELLED;
            case RESOLVED -> to == TicketStatus.CLOSED;
            case CLOSED, CANCELLED -> false;
        };
    }
}
