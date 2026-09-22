package com.enterprise.ai.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Ticket aggregate: identity, fields, title invariant, status lifecycle, and comments.
 * Does not depend on Spring, HTTP, or persistence.
 */
public final class Ticket {

    private final TicketId id;
    private String title;
    private String description;
    private String priority;
    private TicketStatus status;
    private String assignee;
    private final List<Comment> comments = new ArrayList<>();

    private Ticket(
            TicketId id,
            String title,
            String description,
            String priority,
            TicketStatus status,
            String assignee) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.assignee = assignee;
    }

    /**
     * Creates a ticket. Stored status is always {@code OPEN}. Identity is a system-generated
     * UUID string supplied by the caller. Description, priority, and assignee are stored as given;
     * optionality and the priority set remain open questions.
     */
    public static Ticket create(
            TicketId id, String title, String description, String priority, String assignee) {
        Objects.requireNonNull(id, "id");
        return new Ticket(id, requireTitle(title), description, priority, TicketStatus.OPEN, assignee);
    }

    public static Ticket create(TicketId id, String title) {
        return create(id, title, null, null, null);
    }

    /**
     * Restores a previously accepted ticket from persistence. Does not apply create rules
     * (status remains the stored value). Not an HTTP or JPA type.
     */
    public static Ticket reconstitute(
            TicketId id,
            String title,
            String description,
            String priority,
            TicketStatus status,
            String assignee,
            List<Comment> comments) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(status, "status");
        Ticket ticket = new Ticket(id, title, description, priority, status, assignee);
        if (comments != null) {
            ticket.comments.addAll(comments);
        }
        return ticket;
    }

    public TicketId id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public String priority() {
        return priority;
    }

    public TicketStatus status() {
        return status;
    }

    public String assignee() {
        return assignee;
    }

    /**
     * Comments on this ticket only. Display order is OQ-011 and is not specified here.
     */
    public List<Comment> comments() {
        return List.copyOf(comments);
    }

    public void changeTitle(String newTitle) {
        this.title = requireTitle(newTitle);
    }

    /**
     * Description optionality and blank policy are OQ-002 / OQ-004; no extra rule is applied.
     */
    public void changeDescription(String newDescription) {
        this.description = newDescription;
    }

    /**
     * Recognized priority set is OQ-001; VAL-002 is not enforced until that set exists.
     */
    public void changePriority(String newPriority) {
        this.priority = newPriority;
    }

    /**
     * Empty/unassign policy is OQ-003; no extra rule is applied.
     */
    public void changeAssignee(String newAssignee) {
        this.assignee = newAssignee;
    }

    /**
     * Applies a status change. Check order matches {@code spec/state-machine.md} §6:
     * VAL-005, then OQ-012 if same status, then T1–T5 vs illegal pair.
     * On rejection the current status is left unchanged.
     */
    public void changeStatus(String targetToken) {
        changeStatus(TicketStatus.fromToken(targetToken));
    }

    public void changeStatus(TicketStatus target) {
        TicketStatus previous = this.status;
        if (!TicketLifecycle.isAllowed(previous, target)) {
            throw new IllegalStatusTransitionException(previous, target);
        }
        this.status = target;
    }

    /**
     * Adds a comment to this ticket. Creation time is system-assigned by the caller (clock
     * precision deferred). Comments on {@code CLOSED}/{@code CANCELLED} are OQ-009 and are
     * neither allowed nor rejected as a product rule.
     */
    public Comment addComment(String content, String author, Instant creationTime) {
        String validContent = requireCommentContent(content);
        String validAuthor = requireCommentAuthor(author);
        if (creationTime == null) {
            throw new IllegalArgumentException("Comment creation time must be assigned by the system");
        }
        if (status.isTerminal()) {
            throw new OpenQuestionDeferredException(
                    "OQ-009",
                    "Adding comments to CLOSED or CANCELLED tickets is not specified");
        }
        Comment comment = new Comment(id, validContent, validAuthor, creationTime);
        comments.add(comment);
        return comment;
    }

    private static String requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new DomainValidationException(
                    "title", "VAL-001", "Title must be present and not blank");
        }
        return title;
    }

    private static String requireCommentContent(String content) {
        if (content == null || content.isBlank()) {
            throw new DomainValidationException(
                    "content", "VAL-003", "Comment content must be present and not blank");
        }
        return content;
    }

    private static String requireCommentAuthor(String author) {
        if (author == null || author.isBlank()) {
            throw new DomainValidationException(
                    "author", "VAL-004", "Comment author must be present and not blank");
        }
        return author;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Ticket other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
