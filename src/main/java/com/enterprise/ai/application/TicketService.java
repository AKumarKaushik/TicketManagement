package com.enterprise.ai.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enterprise.ai.domain.Comment;
import com.enterprise.ai.domain.DomainValidationException;
import com.enterprise.ai.domain.OpenQuestionDeferredException;
import com.enterprise.ai.domain.Ticket;
import com.enterprise.ai.domain.TicketId;
import com.enterprise.ai.domain.TicketKeywordSearch;
import com.enterprise.ai.domain.TicketStatus;

/**
 * Use-case orchestration. Lifecycle and field invariants stay in the domain.
 */
@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository tickets;
    private final Clock clock;

    public TicketService(TicketRepository tickets, Clock clock) {
        this.tickets = tickets;
        this.clock = clock;
    }

    @Transactional
    public Ticket create(String title, String description, String priority, String assignee, String clientAssignedId) {
        if (clientAssignedId != null) {
            throw new DomainValidationException(
                    "id", "REQ-012", "Ticket identity is assigned by the system");
        }
        TicketId id = new TicketId(UUID.randomUUID().toString());
        Ticket created = Ticket.create(id, title, description, priority, assignee);
        Ticket saved = tickets.save(created);
        log.info("Created ticket {}", saved.id().value());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Ticket> list(String statusToken, String keyword) {
        if (keyword != null && statusToken != null) {
            throw new OpenQuestionDeferredException(
                    "OQ-007",
                    "Combined keyword search and status filter is not specified");
        }
        if (keyword != null) {
            if (keyword.isBlank()) {
                throw new OpenQuestionDeferredException(
                        "OQ-006", "Blank keyword search behavior is not specified");
            }
            return TicketKeywordSearch.matching(tickets.findAll(), keyword);
        }
        if (statusToken != null) {
            TicketStatus status = TicketStatus.fromToken(statusToken);
            return tickets.findByStatus(status);
        }
        return tickets.findAll();
    }

    @Transactional(readOnly = true)
    public Ticket get(String rawId) {
        return requireTicket(rawId);
    }

    @Transactional
    public Ticket update(
            String rawId,
            boolean titlePresent,
            String title,
            boolean descriptionPresent,
            String description,
            boolean priorityPresent,
            String priority,
            boolean assigneePresent,
            String assignee,
            boolean statusPresent,
            boolean idPresent) {
        if (statusPresent) {
            throw new DomainValidationException(
                    "status", "REQ-011", "Status changes must use POST /tickets/{id}/status");
        }
        if (idPresent) {
            throw new DomainValidationException(
                    "id", "REQ-012", "Ticket identity cannot be changed");
        }
        if (!titlePresent && !descriptionPresent && !priorityPresent && !assigneePresent) {
            throw new DomainValidationException(
                    "request",
                    "REQ-004",
                    "At least one of title, description, priority, or assignee must be present");
        }
        Ticket ticket = requireTicket(rawId);
        if (titlePresent) {
            ticket.changeTitle(title);
        }
        if (descriptionPresent) {
            ticket.changeDescription(description);
        }
        if (priorityPresent) {
            ticket.changePriority(priority);
        }
        if (assigneePresent) {
            ticket.changeAssignee(assignee);
        }
        return tickets.save(ticket);
    }

    @Transactional
    public Comment addComment(String rawId, String content, String author, boolean creationTimePresent) {
        if (creationTimePresent) {
            throw new DomainValidationException(
                    "creationTime", "REQ-024", "Comment creation time is assigned by the system");
        }
        Ticket ticket = requireTicket(rawId);
        Comment comment = ticket.addComment(content, author, Instant.now(clock));
        tickets.save(ticket);
        log.info("Added comment on ticket {}", ticket.id().value());
        return comment;
    }

    @Transactional
    public Ticket changeStatus(String rawId, String targetToken) {
        Ticket ticket = requireTicket(rawId);
        ticket.changeStatus(targetToken);
        Ticket saved = tickets.save(ticket);
        log.info("Changed status of ticket {} to {}", saved.id().value(), saved.status());
        return saved;
    }

    private Ticket requireTicket(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            throw new TicketNotFoundException();
        }
        TicketId id = new TicketId(rawId);
        return tickets.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
    }
}
