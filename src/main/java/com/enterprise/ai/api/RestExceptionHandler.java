package com.enterprise.ai.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.enterprise.ai.api.dto.ApiErrorResponse;
import com.enterprise.ai.api.dto.FieldErrorResponse;
import com.enterprise.ai.application.TicketNotFoundException;
import com.enterprise.ai.domain.DomainValidationException;
import com.enterprise.ai.domain.IllegalStatusTransitionException;
import com.enterprise.ai.domain.OpenQuestionDeferredException;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);
    private static final String UNEXPECTED_MESSAGE = "The request could not be completed.";

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ApiErrorResponse> validation(DomainValidationException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION",
                        ex.getMessage(),
                        List.of(new FieldErrorResponse(ex.field(), ex.getMessage()))));
    }

    @ExceptionHandler(IllegalStatusTransitionException.class)
    public ResponseEntity<ApiErrorResponse> illegalTransition(IllegalStatusTransitionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        IllegalStatusTransitionException.ERROR_CODE,
                        ex.getMessage(),
                        null));
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(TicketNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "NOT_FOUND",
                        "Ticket was not found",
                        null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> beanValidation(MethodArgumentNotValidException ex) {
        List<FieldErrorResponse> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorResponse(
                        error.getField(),
                        error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value"))
                .toList();
        String message = fields.isEmpty() ? "The request is invalid." : fields.getFirst().message();
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(
                        HttpStatus.BAD_REQUEST.value(), "VALIDATION", message, fields));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<ApiErrorResponse> malformed(Exception ex) {
        log.info("Rejected unreadable or unsupported request: {}", ex.getClass().getSimpleName());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION",
                        "The request body is invalid.",
                        null));
    }

    @ExceptionHandler(OpenQuestionDeferredException.class)
    public ResponseEntity<ApiErrorResponse> deferred(OpenQuestionDeferredException ex) {
        log.warn("Blocked unspecified operation {}: {}", ex.openQuestionId(), ex.getMessage());
        return unexpected();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> unexpected(Exception ex) {
        log.error("Unexpected failure", ex);
        return unexpected();
    }

    private ResponseEntity<ApiErrorResponse> unexpected() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "UNEXPECTED",
                        UNEXPECTED_MESSAGE,
                        null));
    }
}
