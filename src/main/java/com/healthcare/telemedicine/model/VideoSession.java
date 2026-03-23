package com.healthcare.telemedicine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "video_sessions")
public class VideoSession {
    @Id
    private String id;
    private String appointmentId;
    private String roomId;
    private String jitsiUrl;
    private Instant startTime;
    private Instant endTime;
    private String status; // PENDING, ACTIVE, ENDED
}
