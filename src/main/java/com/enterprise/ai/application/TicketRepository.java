package com.enterprise.ai.application;

import java.util.List;
import java.util.Optional;

import com.enterprise.ai.domain.Ticket;
import com.enterprise.ai.domain.TicketId;
import com.enterprise.ai.domain.TicketStatus;

/**
 * Persistence port. Adapters must not encode lifecycle rules.
 */
public interface TicketRepository {

    Ticket save(Ticket ticket);

    Optional<Ticket> findById(TicketId id);

    List<Ticket> findAll();

    List<Ticket> findByStatus(TicketStatus status);

    void deleteAll();
}
