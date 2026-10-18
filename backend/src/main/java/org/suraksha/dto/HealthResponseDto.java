package org.suraksha.dto;

import java.time.Instant;

/**
 * Health check DTO reporting service liveness and database connectivity.
 */
public class HealthResponseDto {

    private String serviceStatus;
    private String serviceName;
    private String databaseStatus;
    private String databaseDetail;
    private Instant timestamp;

    public HealthResponseDto() {
        this.timestamp = Instant.now();
    }

    public HealthResponseDto(String serviceStatus, String serviceName, String databaseStatus, String databaseDetail) {
        this.serviceStatus = serviceStatus;
        this.serviceName = serviceName;
        this.databaseStatus = databaseStatus;
        this.databaseDetail = databaseDetail;
        this.timestamp = Instant.now();
    }

    public String getServiceStatus() { return serviceStatus; }
    public void setServiceStatus(String serviceStatus) { this.serviceStatus = serviceStatus; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getDatabaseStatus() { return databaseStatus; }
    public void setDatabaseStatus(String databaseStatus) { this.databaseStatus = databaseStatus; }

    public String getDatabaseDetail() { return databaseDetail; }
    public void setDatabaseDetail(String databaseDetail) { this.databaseDetail = databaseDetail; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
