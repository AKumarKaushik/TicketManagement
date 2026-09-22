package com.enterprise.ai.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CommentTest {

    private static final Instant CREATED_AT = Instant.parse("2026-09-21T08:00:00Z");

    @Test
    void addComment_storesContentAuthorCreationTimeOnThatTicket() {
        Ticket ticket = openTicket("t-a");

        Comment comment = ticket.addComment("Rebooted the router", "alice", CREATED_AT);

        assertEquals(ticket.id(), comment.ticketId());
        assertEquals("Rebooted the router", comment.content());
        assertEquals("alice", comment.author());
        assertEquals(CREATED_AT, comment.creationTime());
        assertEquals(List.of(comment), ticket.comments());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void addComment_rejectsBlankContentAndStoresNothing(String content) {
        Ticket ticket = openTicket("t-b");

        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> ticket.addComment(content, "alice", CREATED_AT));

        assertEquals("content", ex.field());
        assertEquals("VAL-003", ex.ruleId());
        assertTrue(ticket.comments().isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void addComment_rejectsBlankAuthorAndStoresNothing(String author) {
        Ticket ticket = openTicket("t-c");

        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> ticket.addComment("Looking into this", author, CREATED_AT));

        assertEquals("author", ex.field());
        assertEquals("VAL-004", ex.ruleId());
        assertTrue(ticket.comments().isEmpty());
    }

    @Test
    void addComment_keepsExistingCommentsOnTheSameTicket() {
        Ticket ticket = openTicket("t-d");
        Comment first = ticket.addComment("First note", "alice", CREATED_AT);

        Comment second = ticket.addComment("Second note", "bob", Instant.parse("2026-09-21T09:00:00Z"));

        assertEquals(2, ticket.comments().size());
        assertTrue(ticket.comments().contains(first));
        assertTrue(ticket.comments().contains(second));
    }

    @Test
    void commentBelongsOnlyToItsTicket() {
        Ticket ticketA = openTicket("t-e");
        Ticket ticketB = openTicket("t-f");

        Comment onA = ticketA.addComment("Only on A", "alice", CREATED_AT);

        assertEquals(ticketA.id(), onA.ticketId());
        assertEquals(List.of(onA), ticketA.comments());
        assertTrue(ticketB.comments().isEmpty());
    }

    @Test
    void addComment_requiresATicketAndCannotExistWithoutAssociation() {
        Ticket ticket = openTicket("t-g");
        Comment comment = ticket.addComment("Needs the ticket", "alice", CREATED_AT);

        assertEquals(ticket.id(), comment.ticketId());
        assertTrue(ticket.comments().contains(comment));
    }

    @Test
    void addComment_doesNotChangeTicketFields() {
        Ticket ticket = Ticket.create(
                new TicketId("t-h"), "Network outage", "VPN down", "unspecified", "alice");

        ticket.addComment("Noted", "bob", CREATED_AT);

        assertEquals("Network outage", ticket.title());
        assertEquals("VPN down", ticket.description());
        assertEquals("unspecified", ticket.priority());
        assertEquals("alice", ticket.assignee());
        assertEquals(TicketStatus.OPEN, ticket.status());
    }

    @Test
    void addComment_rejectsBlankContent() {
        Ticket ticket = openTicket("t-blank-content");
        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> ticket.addComment("", "alice", CREATED_AT));
        assertEquals("VAL-003", ex.ruleId());
        assertTrue(ticket.comments().isEmpty());
    }

    @Test
    void addComment_rejectsWhitespaceOnlyContent() {
        Ticket ticket = openTicket("t-ws-content");
        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> ticket.addComment("   ", "alice", CREATED_AT));
        assertEquals("VAL-003", ex.ruleId());
        assertTrue(ticket.comments().isEmpty());
    }

    @Test
    void addComment_rejectsBlankAuthor() {
        Ticket ticket = openTicket("t-blank-author");
        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> ticket.addComment("Looking into this", "", CREATED_AT));
        assertEquals("VAL-004", ex.ruleId());
        assertTrue(ticket.comments().isEmpty());
    }

    @Test
    void addComment_rejectsWhitespaceOnlyAuthor() {
        Ticket ticket = openTicket("t-ws-author");
        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> ticket.addComment("Looking into this", "\t", CREATED_AT));
        assertEquals("VAL-004", ex.ruleId());
        assertTrue(ticket.comments().isEmpty());
    }

    /*
     * OQ-009: comments on CLOSED / CANCELLED are not classified as allowed or rejected.
     * Do not add pass/fail assertions for terminal-ticket commenting.
     */

    private static Ticket openTicket(String id) {
        return Ticket.create(new TicketId(id), "Network outage");
    }
}
