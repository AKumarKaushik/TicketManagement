package com.enterprise.ai.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Remark on exactly one ticket. Content, author, and creation time are immutable.
 * Technical comment identity and display order are deferred (not a business field; OQ-011).
 */
public final class Comment {

    private final TicketId ticketId;
    private final String content;
    private final String author;
    private final Instant creationTime;

    Comment(TicketId ticketId, String content, String author, Instant creationTime) {
        this.ticketId = ticketId;
        this.content = content;
        this.author = author;
        this.creationTime = creationTime;
    }

    /**
     * Restores a previously accepted comment from persistence. Technical JPA identity is not
     * a domain field.
     */
    public static Comment reconstitute(
            TicketId ticketId, String content, String author, Instant creationTime) {
        Objects.requireNonNull(ticketId, "ticketId");
        Objects.requireNonNull(creationTime, "creationTime");
        return new Comment(ticketId, content, author, creationTime);
    }

    public TicketId ticketId() {
        return ticketId;
    }

    public String content() {
        return content;
    }

    public String author() {
        return author;
    }

    public Instant creationTime() {
        return creationTime;
    }
}
