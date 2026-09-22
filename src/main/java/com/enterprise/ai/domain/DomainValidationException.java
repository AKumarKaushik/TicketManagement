package com.enterprise.ai.domain;

/**
 * Field/invariant validation failure (VAL-*). Distinct from illegal lifecycle transitions.
 * HTTP mapping is not a domain concern.
 */
public final class DomainValidationException extends RuntimeException {

    private final String field;
    private final String ruleId;

    public DomainValidationException(String field, String ruleId, String message) {
        super(message);
        this.field = field;
        this.ruleId = ruleId;
    }

    public String field() {
        return field;
    }

    public String ruleId() {
        return ruleId;
    }
}
