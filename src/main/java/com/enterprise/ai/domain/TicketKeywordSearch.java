package com.enterprise.ai.domain;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Deferred boundary for keyword search [REQ-009]. Matching fields, case, and exact vs partial
 * comparison are OQ-005 and must not be invented. Blank keyword is OQ-006. Combination with
 * status filter is OQ-007.
 */
public final class TicketKeywordSearch {

    private TicketKeywordSearch() {
    }

    /**
     * Does not search. Always reports OQ-005 so no guessed {@code LIKE}/field rule can be used.
     */
    public static List<Ticket> matching(Collection<Ticket> tickets, String keyword) {
        Objects.requireNonNull(tickets, "tickets");
        throw new OpenQuestionDeferredException(
                "OQ-005",
                "Keyword search matching (which fields, case sensitivity, exact vs partial) is not specified");
    }
}
