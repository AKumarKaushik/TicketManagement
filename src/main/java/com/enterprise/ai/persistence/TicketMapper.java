package com.enterprise.ai.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.enterprise.ai.domain.Comment;
import com.enterprise.ai.domain.Ticket;
import com.enterprise.ai.domain.TicketId;
import com.enterprise.ai.domain.TicketStatus;

@Component
class TicketMapper {

    /**
     * Detail/write mapping. Touches the comments collection (JOIN FETCH / entity graph on load).
     */
    Ticket toDomain(TicketEntity entity) {
        return toDomain(entity, true);
    }

    /**
     * List/filter mapping. Does not initialize comments, so listing does not N+1 the collection.
     */
    Ticket toListItem(TicketEntity entity) {
        return toDomain(entity, false);
    }

    private Ticket toDomain(TicketEntity entity, boolean includeComments) {
        TicketId ticketId = new TicketId(entity.getId().toString());
        List<Comment> comments = List.of();
        if (includeComments) {
            comments = new ArrayList<>();
            for (CommentEntity commentEntity : entity.getComments()) {
                comments.add(Comment.reconstitute(
                        ticketId,
                        commentEntity.getContent(),
                        commentEntity.getAuthor(),
                        commentEntity.getCreationTime()));
            }
        }
        return Ticket.reconstitute(
                ticketId,
                entity.getTitle(),
                entity.getDescription(),
                entity.getPriority(),
                TicketStatus.valueOf(entity.getStatus()),
                entity.getAssignee(),
                comments);
    }

    void copyToEntity(Ticket ticket, TicketEntity entity) {
        entity.setId(UUID.fromString(ticket.id().value()));
        entity.setTitle(ticket.title());
        entity.setDescription(ticket.description());
        entity.setPriority(ticket.priority());
        entity.setStatus(ticket.status().name());
        entity.setAssignee(ticket.assignee());
        int alreadyStored = entity.getComments().size();
        List<Comment> domainComments = ticket.comments();
        for (int i = alreadyStored; i < domainComments.size(); i++) {
            Comment comment = domainComments.get(i);
            CommentEntity commentEntity = new CommentEntity();
            commentEntity.setContent(comment.content());
            commentEntity.setAuthor(comment.author());
            commentEntity.setCreationTime(comment.creationTime());
            entity.addComment(commentEntity);
        }
    }
}
