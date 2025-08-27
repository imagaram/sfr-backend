package com.sfr.tokyo.sfr_backend.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // 4xx - validation / format
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation error"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad request"),

    // 4xx - domain specific business rules
    ELECTION_PHASE_INVALID(HttpStatus.BAD_REQUEST, "Election phase invalid"),
    ELECTION_TIME_WINDOW(HttpStatus.BAD_REQUEST, "Outside election time window"),
    DUPLICATE_CANDIDATE(HttpStatus.CONFLICT, "Candidate already registered"),
    DUPLICATE_VOTE(HttpStatus.BAD_REQUEST, "Already voted"),
    RESULTS_UNAVAILABLE(HttpStatus.BAD_REQUEST, "Results not available yet"),
    MANIFESTO_EDIT_CLOSED(HttpStatus.BAD_REQUEST, "Manifesto editing closed"),
    MANIFESTO_QA_CLOSED(HttpStatus.BAD_REQUEST, "Manifesto Q&A closed"),
    VOTER_INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "Insufficient SFR balance"),
    VOTER_INSUFFICIENT_ACTIVITY(HttpStatus.BAD_REQUEST, "Insufficient activity score"),

    // generic business fallback
    BUSINESS_RULE_VIOLATION(HttpStatus.BAD_REQUEST, "Business rule violation"),

    // 4xx - resource
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    ELECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Election not found"),
    CANDIDATE_NOT_FOUND(HttpStatus.NOT_FOUND, "Candidate not found"),
    MANIFESTO_NOT_FOUND(HttpStatus.NOT_FOUND, "Manifesto not found"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied"),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid credentials"),

    // 5xx
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() { return status; }
    public String getDefaultMessage() { return defaultMessage; }
}
