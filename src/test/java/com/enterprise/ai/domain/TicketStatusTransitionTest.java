package com.enterprise.ai.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TicketStatusTransitionTest {

    /*
     * Transition IDs follow spec/state-machine.md (not a remapped T1–T5 list):
     * T1 OPEN→IN_PROGRESS, T2 IN_PROGRESS→RESOLVED, T3 RESOLVED→CLOSED,
     * T4 OPEN→CANCELLED, T5 IN_PROGRESS→CANCELLED.
     * Same-status pairs are OQ-012 and are not asserted as pass or fail.
     */

    @Test
    void t1_openToInProgress_leavesCommentsUnchanged() {
        Ticket ticket = openTicket();
        Comment note = ticket.addComment(
                "Started diagnosis", "alice", Instant.parse("2026-09-21T08:00:00Z"));

        ticket.changeStatus(TicketStatus.IN_PROGRESS);

        assertEquals(TicketStatus.IN_PROGRESS, ticket.status());
        assertEquals(List.of(note), ticket.comments());
    }

    @Test
    void invalidTransition_isBusinessErrorNotValidation() {
        Ticket ticket = openTicket();

        IllegalStatusTransitionException ex = assertThrows(
                IllegalStatusTransitionException.class,
                () -> ticket.changeStatus(TicketStatus.RESOLVED));

        assertEquals(IllegalStatusTransitionException.ERROR_CODE, ex.errorCode());
        assertEquals(TicketStatus.OPEN, ticket.status());
    }

    @Test
    void t1_openToInProgress() {
        Ticket ticket = openTicket();
        ticket.changeStatus(TicketStatus.IN_PROGRESS);
        assertEquals(TicketStatus.IN_PROGRESS, ticket.status());
        assertUnchangedFields(ticket);
    }

    @Test
    void t2_inProgressToResolved() {
        Ticket ticket = ticketAt(TicketStatus.IN_PROGRESS);
        ticket.changeStatus(TicketStatus.RESOLVED);
        assertEquals(TicketStatus.RESOLVED, ticket.status());
        assertUnchangedFields(ticket);
    }

    @Test
    void t3_resolvedToClosed() {
        Ticket ticket = ticketAt(TicketStatus.RESOLVED);
        ticket.changeStatus(TicketStatus.CLOSED);
        assertEquals(TicketStatus.CLOSED, ticket.status());
        assertUnchangedFields(ticket);
    }

    @Test
    void t4_openToCancelled() {
        Ticket ticket = openTicket();
        ticket.changeStatus(TicketStatus.CANCELLED);
        assertEquals(TicketStatus.CANCELLED, ticket.status());
        assertUnchangedFields(ticket);
    }

    @Test
    void t5_inProgressToCancelled() {
        Ticket ticket = ticketAt(TicketStatus.IN_PROGRESS);
        ticket.changeStatus(TicketStatus.CANCELLED);
        assertEquals(TicketStatus.CANCELLED, ticket.status());
        assertUnchangedFields(ticket);
    }

    @ParameterizedTest(name = "invalid {0} -> {1} leaves status unchanged")
    @CsvSource({
        "OPEN, RESOLVED",
        "OPEN, CLOSED",
        "IN_PROGRESS, OPEN",
        "IN_PROGRESS, CLOSED",
        "RESOLVED, OPEN",
        "RESOLVED, IN_PROGRESS",
        "RESOLVED, CANCELLED",
        "CLOSED, OPEN",
        "CLOSED, IN_PROGRESS",
        "CLOSED, RESOLVED",
        "CLOSED, CANCELLED",
        "CANCELLED, OPEN",
        "CANCELLED, IN_PROGRESS",
        "CANCELLED, RESOLVED",
        "CANCELLED, CLOSED"
    })
    void invalidTransition_isRejectedAndStatusUnchanged(TicketStatus from, TicketStatus to) {
        Ticket ticket = ticketAt(from);

        IllegalStatusTransitionException ex =
                assertThrows(IllegalStatusTransitionException.class, () -> ticket.changeStatus(to));

        assertEquals(IllegalStatusTransitionException.ERROR_CODE, ex.errorCode());
        assertEquals(from, ex.currentStatus());
        assertEquals(to, ex.targetStatus());
        assertEquals(from, ticket.status());
        assertUnchangedFields(ticket);
    }

    @Test
    void changeStatus_unknownToken_isValidationNotTransition() {
        Ticket ticket = openTicket();

        DomainValidationException ex =
                assertThrows(DomainValidationException.class, () -> ticket.changeStatus("UNKNOWN"));

        assertEquals("status", ex.field());
        assertEquals("VAL-005", ex.ruleId());
        assertEquals(TicketStatus.OPEN, ticket.status());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "open", "in_progress", "DONE"})
    void changeStatus_unrecognizedToken_isValidationAndLeavesStatus(String token) {
        Ticket ticket = openTicket();

        DomainValidationException ex =
                assertThrows(DomainValidationException.class, () -> ticket.changeStatus(token));

        assertEquals("VAL-005", ex.ruleId());
        assertEquals(TicketStatus.OPEN, ticket.status());
    }

    @Test
    void closedAndCancelled_areTerminalAgainstEveryOtherStatus() {
        for (TicketStatus target : TicketStatus.values()) {
            if (target == TicketStatus.CLOSED) {
                continue;
            }
            Ticket closed = ticketAt(TicketStatus.CLOSED);
            assertThrows(IllegalStatusTransitionException.class, () -> closed.changeStatus(target));
            assertEquals(TicketStatus.CLOSED, closed.status());
        }
        for (TicketStatus target : TicketStatus.values()) {
            if (target == TicketStatus.CANCELLED) {
                continue;
            }
            Ticket cancelled = ticketAt(TicketStatus.CANCELLED);
            assertThrows(IllegalStatusTransitionException.class, () -> cancelled.changeStatus(target));
            assertEquals(TicketStatus.CANCELLED, cancelled.status());
        }
    }

    /*
     * OQ-012: same-status is not classified as T1–T5 or as an invalid pair.
     * Do not add pass/fail assertions for OPEN→OPEN (or any status → itself).
     */

    private static Ticket openTicket() {
        return Ticket.create(
                new TicketId("t-lifecycle"),
                "Network outage",
                "VPN down",
                "unspecified",
                "alice");
    }

    private static Ticket ticketAt(TicketStatus status) {
        Ticket ticket = openTicket();
        switch (status) {
            case OPEN -> {
            }
            case IN_PROGRESS -> ticket.changeStatus(TicketStatus.IN_PROGRESS);
            case RESOLVED -> {
                ticket.changeStatus(TicketStatus.IN_PROGRESS);
                ticket.changeStatus(TicketStatus.RESOLVED);
            }
            case CLOSED -> {
                ticket.changeStatus(TicketStatus.IN_PROGRESS);
                ticket.changeStatus(TicketStatus.RESOLVED);
                ticket.changeStatus(TicketStatus.CLOSED);
            }
            case CANCELLED -> ticket.changeStatus(TicketStatus.CANCELLED);
        }
        return ticket;
    }

    private static void assertUnchangedFields(Ticket ticket) {
        assertEquals("t-lifecycle", ticket.id().value());
        assertEquals("Network outage", ticket.title());
        assertEquals("VPN down", ticket.description());
        assertEquals("unspecified", ticket.priority());
        assertEquals("alice", ticket.assignee());
    }
}
