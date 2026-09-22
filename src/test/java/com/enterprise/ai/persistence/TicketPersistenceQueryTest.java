package com.enterprise.ai.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.enterprise.ai.application.TicketService;
import com.enterprise.ai.application.TicketRepository;
import com.enterprise.ai.domain.Ticket;
import com.enterprise.ai.domain.TicketStatus;

@SpringBootTest
@ActiveProfiles("test")
class TicketPersistenceQueryTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository tickets;

    @Autowired
    private TicketJpaRepository jpa;

    private TransactionTemplate transactions;

    @Autowired
    void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @BeforeEach
    void clearStore() {
        tickets.deleteAll();
    }

    @Test
    void findByStatus_queriesMatchingRowsOnly() {
        Ticket open = ticketService.create("Open ticket", null, null, null, null);
        Ticket moving = ticketService.create("In progress ticket", null, null, null, null);
        ticketService.changeStatus(moving.id().value(), "IN_PROGRESS");

        List<Ticket> filtered = tickets.findByStatus(TicketStatus.OPEN);

        assertEquals(1, filtered.size());
        assertEquals(open.id(), filtered.getFirst().id());
        assertEquals(TicketStatus.OPEN, ticketService.get(open.id().value()).status());
        assertEquals(TicketStatus.IN_PROGRESS, ticketService.get(moving.id().value()).status());
    }

    @Test
    void findAll_doesNotInitializeComments() {
        Ticket created = ticketService.create("Network outage", null, null, null, null);
        ticketService.addComment(created.id().value(), "Checking routers", "bob", false);

        transactions.executeWithoutResult(status -> {
            List<TicketEntity> listed = jpa.findAll();
            assertEquals(1, listed.size());
            assertFalse(Hibernate.isInitialized(listed.getFirst().getComments()));
        });

        Ticket detailed = ticketService.get(created.id().value());
        assertEquals(1, detailed.comments().size());
        assertEquals("Checking routers", detailed.comments().getFirst().content());
    }

    @Test
    void findByStatus_doesNotInitializeComments() {
        Ticket created = ticketService.create("Network outage", null, null, null, null);
        ticketService.addComment(created.id().value(), "Checking routers", "bob", false);

        transactions.executeWithoutResult(status -> {
            List<TicketEntity> listed = jpa.findByStatus(TicketStatus.OPEN.name());
            assertEquals(1, listed.size());
            assertFalse(Hibernate.isInitialized(listed.getFirst().getComments()));
        });
    }

    @Test
    void findWithCommentsById_loadsComments() {
        Ticket created = ticketService.create("Network outage", null, null, null, null);
        ticketService.addComment(created.id().value(), "Checking routers", "bob", false);

        transactions.executeWithoutResult(status -> {
            TicketEntity detailed =
                    jpa.findWithCommentsById(UUID.fromString(created.id().value())).orElseThrow();
            assertTrue(Hibernate.isInitialized(detailed.getComments()));
            assertEquals(1, detailed.getComments().size());
        });
    }

    @Test
    void findByStatus_doesNotMutateOtherTickets() {
        Ticket open = ticketService.create("Must stay open", null, null, null, null);
        tickets.findByStatus(TicketStatus.CLOSED);
        assertEquals(TicketStatus.OPEN, ticketService.get(open.id().value()).status());
        assertEquals("Must stay open", ticketService.get(open.id().value()).title());
    }
}
