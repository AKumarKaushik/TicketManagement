package com.enterprise.ai.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateTicketRequest {

    private String title;
    private boolean titlePresent;
    private String description;
    private boolean descriptionPresent;
    private String priority;
    private boolean priorityPresent;
    private String assignee;
    private boolean assigneePresent;
    private boolean statusPresent;
    private boolean idPresent;

    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.titlePresent = true;
        this.title = title;
    }

    public boolean isTitlePresent() {
        return titlePresent;
    }

    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.descriptionPresent = true;
        this.description = description;
    }

    public boolean isDescriptionPresent() {
        return descriptionPresent;
    }

    public String getPriority() {
        return priority;
    }

    @JsonProperty("priority")
    public void setPriority(String priority) {
        this.priorityPresent = true;
        this.priority = priority;
    }

    public boolean isPriorityPresent() {
        return priorityPresent;
    }

    public String getAssignee() {
        return assignee;
    }

    @JsonProperty("assignee")
    public void setAssignee(String assignee) {
        this.assigneePresent = true;
        this.assignee = assignee;
    }

    public boolean isAssigneePresent() {
        return assigneePresent;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.statusPresent = true;
    }

    public boolean isStatusPresent() {
        return statusPresent;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.idPresent = true;
    }

    public boolean isIdPresent() {
        return idPresent;
    }
}
