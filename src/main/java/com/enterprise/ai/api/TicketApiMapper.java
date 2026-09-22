package com.enterprise.ai.api;

import java.util.List;

import org.springframework.stereotype.Component;

import com.enterprise.ai.api.dto.CommentResponse;
import com.enterprise.ai.api.dto.TicketDetailsResponse;
import com.enterprise.ai.api.dto.TicketResponse;
import com.enterprise.ai.domain.Comment;
import com.enterprise.ai.domain.Ticket;

@Component
class TicketApiMapper {

    TicketResponse toSummary(Ticket ticket) {
        return new TicketResponse(
                ticket.id().value(),
                ticket.title(),
                ticket.description(),
                ticket.priority(),
                ticket.status().name(),
                ticket.assignee());
    }

    TicketDetailsResponse toDetails(Ticket ticket) {
        return new TicketDetailsResponse(
                ticket.id().value(),
                ticket.title(),
                ticket.description(),
                ticket.priority(),
                ticket.status().name(),
                ticket.assignee(),
                ticket.comments().stream().map(this::toComment).toList());
    }

    CommentResponse toComment(Comment comment) {
        return new CommentResponse(
                comment.content(), comment.author(), comment.creationTime().toString());
    }

    List<TicketResponse> toSummaries(List<Ticket> tickets) {
        return tickets.stream().map(this::toSummary).toList();
    }
}
