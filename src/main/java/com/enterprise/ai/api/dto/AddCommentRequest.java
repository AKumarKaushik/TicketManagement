package com.enterprise.ai.api.dto;

import jakarta.validation.constraints.NotBlank;

public class AddCommentRequest {

    @NotBlank(message = "Comment content must be present and not blank")
    private String content;
    @NotBlank(message = "Comment author must be present and not blank")
    private String author;
    private String creationTime;
    private boolean creationTimePresent;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
        this.creationTimePresent = true;
    }

    public boolean isCreationTimePresent() {
        return creationTimePresent;
    }
}
