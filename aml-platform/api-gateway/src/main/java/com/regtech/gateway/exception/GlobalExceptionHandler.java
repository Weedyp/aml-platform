package com.regtech.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Catch-all handler for unexpected internal server errors.
     * Prevents raw Java stack traces from leaking to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal error occurred on the server."
        );

        problemDetail.setType(URI.create("https://api.regtech.com/errors/internal-server-error"));
        problemDetail.setTitle("Internal Server Error");

        // RFC 7807 allows extending the object with custom fields
        problemDetail.setProperty("timestamp", Instant.now());

        return new ResponseEntity<>(problemDetail, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handler for business rules violations or invalid system states.
     * Useful for validation steps or database connectivity drops.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        problemDetail.setType(URI.create("https://api.regtech.com/errors/bad-request"));
        problemDetail.setTitle("Invalid Request State");
        problemDetail.setProperty("timestamp", Instant.now());

        return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
    }
}
