package com.healthcare.telemedicine.dto;

import lombok.Data;

@Data
public class CreateSessionRequest {
    private String patientId;
    private String doctorId;
    private Boolean forceNew;
}
