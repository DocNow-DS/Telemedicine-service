package com.healthcare.telemedicine.dto;

import lombok.Data;

@Data
public class ScheduleConsultationRequest {
    private String patientId;
    private String doctorId;
    /** ISO-8601 instant string, e.g. 2026-03-27T05:00:00Z */
    private String scheduledAt;
}
