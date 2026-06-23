package com.regtech.gateway.dto;

import java.time.Instant;

public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final Instant timestamp;

    // Private constructor enforces the use of the Builder or Static Factory Methods
    private ApiResponse(Builder<T> builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.data = builder.data;
        this.timestamp = Instant.now();
    }

    // Getters are strictly required for Jackson to serialize this into JSON
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public Instant getTimestamp() { return timestamp; }

    // --- Static Factory Methods for Quick Operations ---

    public static <T> ApiResponse<T> success(T data, String message) {
        return new Builder<T>().success(true).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation completed successfully");
    }

    public static <T> ApiResponse<T> success(String message) {
        return new Builder<T>().success(true).message(message).build();
    }

    // --- The Builder Pattern for Complex Construction ---

    public static class Builder<T> {
        private boolean success;
        private String message;
        private T data;

        public Builder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public ApiResponse<T> build() {
            return new ApiResponse<>(this);
        }
    }
}
