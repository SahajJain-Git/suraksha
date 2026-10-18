package org.suraksha.service;

import org.springframework.stereotype.Service;
import org.suraksha.dto.HealthResponseDto;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;

@Service
public class HealthService {

    private final DataSource dataSource;

    public HealthService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Checks service liveness and performs an active connection check against the configured DataSource.
     */
    public HealthResponseDto checkHealth() {
        String serviceStatus = "UP";
        String serviceName = "Suraksha Disaster Preparedness Platform API";
        String databaseStatus = "DISCONNECTED";
        String databaseDetail = "No connection attempted";

        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                databaseStatus = "CONNECTED";
                databaseDetail = String.format("Connected to %s (%s)",
                        connection.getMetaData().getDatabaseProductName(),
                        connection.getMetaData().getDatabaseProductVersion());
            } else {
                databaseStatus = "DISCONNECTED";
                databaseDetail = "Database connection validation timed out.";
            }
        } catch (Exception ex) {
            databaseStatus = "DISCONNECTED";
            databaseDetail = "Database connection error: " + ex.getMessage();
        }

        HealthResponseDto dto = new HealthResponseDto(serviceStatus, serviceName, databaseStatus, databaseDetail);
        dto.setTimestamp(Instant.now());
        return dto;
    }
}
