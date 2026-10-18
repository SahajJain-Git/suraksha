package org.suraksha.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard structured error response returned by GlobalExceptionHandler.
 */
public class ErrorResponseDto {

    private boolean success = false;
    private int status;
    private String error;
    private String message;
    private List<String> details = new ArrayList<>();
    private String path;
    private Instant timestamp;

    public ErrorResponseDto() {
        this.timestamp = Instant.now();
    }

    public ErrorResponseDto(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now();
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getDetails() { return details; }
    public void setDetails(List<String> details) { this.details = details; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public void addDetail(String detail) {
        this.details.add(detail);
    }
}
