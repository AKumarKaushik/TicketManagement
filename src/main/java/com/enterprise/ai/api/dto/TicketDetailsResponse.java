package com.enterprise.ai.api.dto;

import java.util.List;

public record TicketDetailsResponse(
        String id,
        String title,
        String description,
        String priority,
        String status,
        String assignee,
        List<CommentResponse> comments) {
}
