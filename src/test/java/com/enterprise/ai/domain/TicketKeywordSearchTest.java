package com.enterprise.ai.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Keyword matching is OQ-005. These tests only prove the deferred boundary exists;
 * they do not assert any field, case, or partial-match rule.
 */
class TicketKeywordSearchTest {

    @Test
    void matching_isDeferredUntilOq005() {
        Ticket ticket = Ticket.create(new TicketId("search-1"), "Network outage");

        OpenQuestionDeferredException ex = assertThrows(
                OpenQuestionDeferredException.class,
                () -> TicketKeywordSearch.matching(List.of(ticket), "Network"));

        assertEquals("OQ-005", ex.openQuestionId());
    }

    @Test
    void matching_doesNotMutateTicketsOrInventAResultList() {
        Ticket ticket = Ticket.create(new TicketId("search-2"), "Network outage");
        List<Ticket> source = new ArrayList<>();
        source.add(ticket);

        assertThrows(
                OpenQuestionDeferredException.class,
                () -> TicketKeywordSearch.matching(source, "Network"));

        assertEquals(1, source.size());
        assertEquals(TicketStatus.OPEN, ticket.status());
        assertEquals("Network outage", ticket.title());
    }
}
