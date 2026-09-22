package com.enterprise.ai.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.enterprise.ai.application.TicketRepository;
import com.enterprise.ai.domain.Ticket;
import com.enterprise.ai.domain.TicketId;
import com.enterprise.ai.domain.TicketStatus;

@Repository
class TicketRepositoryAdapter implements TicketRepository {

    private final TicketJpaRepository jpa;
    private final TicketMapper mapper;

    TicketRepositoryAdapter(TicketJpaRepository jpa, TicketMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Ticket save(Ticket ticket) {
        UUID id = UUID.fromString(ticket.id().value());
        TicketEntity entity = jpa.findById(id).orElseGet(TicketEntity::new);
        mapper.copyToEntity(ticket, entity);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Ticket> findById(TicketId id) {
        return parseUuid(id).flatMap(jpa::findWithCommentsById).map(mapper::toDomain);
    }

    @Override
    public List<Ticket> findAll() {
        return jpa.findAll().stream().map(mapper::toListItem).toList();
    }

    @Override
    public List<Ticket> findByStatus(TicketStatus status) {
        return jpa.findByStatus(status.name()).stream().map(mapper::toListItem).toList();
    }

    @Override
    public void deleteAll() {
        jpa.deleteAll();
    }

    private Optional<UUID> parseUuid(TicketId id) {
        try {
            return Optional.of(UUID.fromString(id.value()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
