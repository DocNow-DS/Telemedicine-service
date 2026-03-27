package com.healthcare.telemedicine.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ConsultationDto {
    private String id;
    private String doctorId;
    private String patientId;
    private String roomId;
    private String jitsiUrl;
    private String status;
    private Instant scheduledAt;
    private Instant createdAt;
    private Instant answeredAt;
    private Instant endedAt;
}
