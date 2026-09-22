package com.enterprise.ai.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TicketTest {

    @Test
    void create_setsStatusOpenAndStoresSuppliedFields() {
        Ticket ticket = Ticket.create(
                new TicketId("t-1"), "Network outage", "VPN down", "unspecified", "alice");

        assertEquals(TicketStatus.OPEN, ticket.status());
        assertEquals("t-1", ticket.id().value());
        assertEquals("Network outage", ticket.title());
        assertEquals("VPN down", ticket.description());
        assertEquals("unspecified", ticket.priority());
        assertEquals("alice", ticket.assignee());
    }

    @Test
    void create_withTitleOnly_startsOpenAndLeavesOptionalFieldsUnset() {
        Ticket ticket = Ticket.create(new TicketId("t-2"), "Printer jam");

        assertEquals(TicketStatus.OPEN, ticket.status());
        assertNull(ticket.description());
        assertNull(ticket.priority());
        assertNull(ticket.assignee());
    }

    @Test
    void create_doesNotAcceptClientChosenStatus() {
        Ticket ticket = Ticket.create(new TicketId("t-3"), "Access request");
        assertEquals(TicketStatus.OPEN, ticket.status());
        assertNotEquals(TicketStatus.CLOSED, ticket.status());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "  \n"})
    void create_rejectsBlankTitle(String title) {
        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> Ticket.create(new TicketId("t-4"), title));
        assertEquals("title", ex.field());
        assertEquals("VAL-001", ex.ruleId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void changeTitle_rejectsBlankTitleAndLeavesPreviousTitle(String title) {
        Ticket ticket = Ticket.create(new TicketId("t-5"), "Original title");

        DomainValidationException ex =
                assertThrows(DomainValidationException.class, () -> ticket.changeTitle(title));

        assertEquals("VAL-001", ex.ruleId());
        assertEquals("Original title", ticket.title());
        assertEquals(TicketStatus.OPEN, ticket.status());
    }

    @Test
    void changeTitle_replacesTitleOnly() {
        Ticket ticket = Ticket.create(
                new TicketId("t-6"), "Old title", "keep-desc", "keep-pri", "keep-assignee");

        ticket.changeTitle("New title");

        assertEquals("New title", ticket.title());
        assertEquals("keep-desc", ticket.description());
        assertEquals("keep-pri", ticket.priority());
        assertEquals("keep-assignee", ticket.assignee());
        assertEquals(TicketStatus.OPEN, ticket.status());
    }

    @Test
    void create_rejectsMissingTitle() {
        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> Ticket.create(new TicketId("t-missing-title"), null));
        assertEquals("title", ex.field());
        assertEquals("VAL-001", ex.ruleId());
    }

    @Test
    void create_rejectsBlankTitle() {
        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> Ticket.create(new TicketId("t-empty-title"), ""));
        assertEquals("VAL-001", ex.ruleId());
    }

    @Test
    void create_rejectsWhitespaceOnlyTitle() {
        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> Ticket.create(new TicketId("t-ws-title"), "   "));
        assertEquals("VAL-001", ex.ruleId());
    }

    @Test
    void create_acceptsValidTitle() {
        Ticket ticket = Ticket.create(new TicketId("t-valid-title"), "Cannot print invoices");
        assertEquals("Cannot print invoices", ticket.title());
        assertEquals(TicketStatus.OPEN, ticket.status());
    }

    @Test
    void ticketId_rejectsBlankValue() {
        assertThrows(IllegalArgumentException.class, () -> new TicketId(" "));
        assertThrows(IllegalArgumentException.class, () -> new TicketId(null));
    }

    @Test
    void fiveStatusesExist() {
        assertEquals(5, TicketStatus.values().length);
        assertTrue(TicketStatus.CLOSED.isTerminal());
        assertTrue(TicketStatus.CANCELLED.isTerminal());
    }

    @Test
    void reconstitute_restoresPersistedStatusAndComments() {
        TicketId id = new TicketId("11111111-1111-1111-1111-111111111111");
        Instant at = Instant.parse("2026-09-21T08:00:00Z");
        Comment note = Comment.reconstitute(id, "Checking routers", "bob", at);

        Ticket ticket = Ticket.reconstitute(
                id,
                "Network outage",
                "VPN down",
                "unspecified",
                TicketStatus.IN_PROGRESS,
                "alice",
                List.of(note));

        assertEquals(TicketStatus.IN_PROGRESS, ticket.status());
        assertEquals("Network outage", ticket.title());
        assertEquals(1, ticket.comments().size());
        assertEquals("Checking routers", ticket.comments().getFirst().content());
    }
}
