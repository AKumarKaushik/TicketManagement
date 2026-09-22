package com.enterprise.ai.api;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enterprise.ai.api.dto.AddCommentRequest;
import com.enterprise.ai.api.dto.ChangeStatusRequest;
import com.enterprise.ai.api.dto.CommentResponse;
import com.enterprise.ai.api.dto.CreateTicketRequest;
import com.enterprise.ai.api.dto.TicketDetailsResponse;
import com.enterprise.ai.api.dto.TicketResponse;
import com.enterprise.ai.api.dto.UpdateTicketRequest;
import com.enterprise.ai.application.TicketService;
import com.enterprise.ai.domain.Comment;
import com.enterprise.ai.domain.Ticket;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService tickets;
    private final TicketApiMapper mapper;

    public TicketController(TicketService tickets, TicketApiMapper mapper) {
        this.tickets = tickets;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
        Ticket created = tickets.create(
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                request.getAssignee(),
                request.getId());
        TicketResponse body = mapper.toSummary(created);
        return ResponseEntity.created(URI.create("/tickets/" + body.id())).body(body);
    }

    @GetMapping
    public List<TicketResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return mapper.toSummaries(tickets.list(status, keyword));
    }

    @GetMapping("/{id}")
    public TicketDetailsResponse get(@PathVariable String id) {
        return mapper.toDetails(tickets.get(id));
    }

    @PatchMapping("/{id}")
    public TicketResponse update(@PathVariable String id, @RequestBody UpdateTicketRequest request) {
        return mapper.toSummary(tickets.update(
                id,
                request.isTitlePresent(),
                request.getTitle(),
                request.isDescriptionPresent(),
                request.getDescription(),
                request.isPriorityPresent(),
                request.getPriority(),
                request.isAssigneePresent(),
                request.getAssignee(),
                request.isStatusPresent(),
                request.isIdPresent()));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable String id, @Valid @RequestBody AddCommentRequest request) {
        Comment comment = tickets.addComment(
                id, request.getContent(), request.getAuthor(), request.isCreationTimePresent());
        CommentResponse body = mapper.toComment(comment);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/tickets/" + id))
                .body(body);
    }

    @PostMapping("/{id}/status")
    public TicketResponse changeStatus(@PathVariable String id, @RequestBody ChangeStatusRequest request) {
        return mapper.toSummary(tickets.changeStatus(id, request.getStatus()));
    }
}
