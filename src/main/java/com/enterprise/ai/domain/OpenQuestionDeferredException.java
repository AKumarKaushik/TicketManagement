package com.enterprise.ai.domain;

/**
 * Raised when a requested operation is still an unanswered open question.
 * This is not a product decision that the operation is valid or invalid.
 */
public final class OpenQuestionDeferredException extends RuntimeException {

    private final String openQuestionId;

    public OpenQuestionDeferredException(String openQuestionId, String message) {
        super(message);
        this.openQuestionId = openQuestionId;
    }

    public String openQuestionId() {
        return openQuestionId;
    }
}
