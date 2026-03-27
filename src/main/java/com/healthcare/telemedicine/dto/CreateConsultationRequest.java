package com.healthcare.telemedicine.dto;

import lombok.Data;

@Data
public class CreateConsultationRequest {
    private String patientId;
    private String doctorId;
}
