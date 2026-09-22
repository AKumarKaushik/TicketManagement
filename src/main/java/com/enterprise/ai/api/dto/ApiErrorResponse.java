package com.enterprise.ai.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        int status, String errorCode, String message, List<FieldErrorResponse> fields) {
}
