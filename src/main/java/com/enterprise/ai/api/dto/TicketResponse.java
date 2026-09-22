package com.enterprise.ai.api.dto;

public record TicketResponse(
        String id,
        String title,
        String description,
        String priority,
        String status,
        String assignee) {
}
