package com.healthcare.telemedicine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoSessionDto {
    private String appointmentId;
    private String roomId;
    private String jitsiUrl;
    private String status;
    private Instant startTime;
}
