package com.enterprise.ai.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TicketStatusFilterTest {

    @ParameterizedTest
    @EnumSource(TicketStatus.class)
    void filter_returnsOnlyTicketsWithThatApprovedStatus(TicketStatus status) {
        List<Ticket> tickets = sampleTickets();

        List<Ticket> filtered = TicketStatusFilter.apply(tickets, status);

        assertEquals(1, filtered.size());
        assertEquals(status, filtered.getFirst().status());
        assertTrue(TicketStatus.valueOf(filtered.getFirst().status().name()) == status);
    }

    @ParameterizedTest
    @EnumSource(TicketStatus.class)
    void filter_tokenForm_usesOnlyApprovedStatuses(TicketStatus status) {
        List<Ticket> filtered = TicketStatusFilter.apply(sampleTickets(), status.name());

        assertEquals(1, filtered.size());
        assertEquals(status, filtered.getFirst().status());
    }

    @Test
    void filter_validStatusWithNoMatches_returnsEmptyNotFailure() {
        Ticket openOnly = Ticket.create(new TicketId("only-open"), "Title");

        List<Ticket> filtered = TicketStatusFilter.apply(List.of(openOnly), TicketStatus.CLOSED);

        assertTrue(filtered.isEmpty());
        assertEquals(TicketStatus.OPEN, openOnly.status());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "open", "UNKNOWN", "DONE"})
    void filter_rejectsInvalidStatusValues(String token) {
        List<Ticket> tickets = sampleTickets();

        DomainValidationException ex = assertThrows(
                DomainValidationException.class, () -> TicketStatusFilter.apply(tickets, token));

        assertEquals("status", ex.field());
        assertEquals("VAL-005", ex.ruleId());
        assertEquals(5, tickets.size());
    }

    @Test
    void filter_doesNotIntroduceAnUnknownStatus() {
        List<Ticket> filtered = TicketStatusFilter.apply(sampleTickets(), TicketStatus.RESOLVED);

        for (Ticket ticket : filtered) {
            TicketStatus.fromToken(ticket.status().name());
            assertEquals(TicketStatus.RESOLVED, ticket.status());
        }
    }

    @Test
    void filter_rejectsBlankStatus() {
        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> TicketStatusFilter.apply(sampleTickets(), ""));
        assertEquals("VAL-005", ex.ruleId());
    }

    @Test
    void filter_rejectsWrongCaseStatus() {
        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> TicketStatusFilter.apply(sampleTickets(), "open"));
        assertEquals("VAL-005", ex.ruleId());
    }

    @Test
    void filter_rejectsUnknownStatus() {
        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> TicketStatusFilter.apply(sampleTickets(), "UNKNOWN"));
        assertEquals("VAL-005", ex.ruleId());
    }

    @Test
    void filter_doesNotMutateTickets() {
        Ticket ticket = Ticket.create(new TicketId("stable"), "Title");
        List<Ticket> source = new ArrayList<>();
        source.add(ticket);

        TicketStatusFilter.apply(source, TicketStatus.OPEN);

        assertEquals(1, source.size());
        assertEquals(TicketStatus.OPEN, ticket.status());
        assertEquals("Title", ticket.title());
    }

    private static List<Ticket> sampleTickets() {
        Ticket open = Ticket.create(new TicketId("s-open"), "Open ticket");

        Ticket inProgress = Ticket.create(new TicketId("s-progress"), "In progress ticket");
        inProgress.changeStatus(TicketStatus.IN_PROGRESS);

        Ticket resolved = Ticket.create(new TicketId("s-resolved"), "Resolved ticket");
        resolved.changeStatus(TicketStatus.IN_PROGRESS);
        resolved.changeStatus(TicketStatus.RESOLVED);

        Ticket closed = Ticket.create(new TicketId("s-closed"), "Closed ticket");
        closed.changeStatus(TicketStatus.IN_PROGRESS);
        closed.changeStatus(TicketStatus.RESOLVED);
        closed.changeStatus(TicketStatus.CLOSED);

        Ticket cancelled = Ticket.create(new TicketId("s-cancelled"), "Cancelled ticket");
        cancelled.changeStatus(TicketStatus.CANCELLED);

        return List.of(open, inProgress, resolved, closed, cancelled);
    }
}
