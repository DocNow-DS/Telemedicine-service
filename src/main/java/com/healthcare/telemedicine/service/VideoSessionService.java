package com.healthcare.telemedicine.service;

import com.healthcare.telemedicine.dto.AppointmentDto;
import com.healthcare.telemedicine.model.VideoSession;
import com.healthcare.telemedicine.repository.VideoSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class VideoSessionService {

    private final VideoSessionRepository videoSessionRepository;
    private final AppointmentServiceClient appointmentServiceClient;

    private static final String JITSI_BASE_URL = "https://meet.jit.si/";

    public VideoSession createSession(String appointmentId) {
        // 1. Fetch appointment details from Appointment Service
        AppointmentDto appointment = appointmentServiceClient.getAppointment(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        // 2. Validate status
        if (!"CONFIRMED".equalsIgnoreCase(appointment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Appointment is not confirmed");
        }

        // 3. Check if session already exists
        return videoSessionRepository.findByAppointmentId(appointmentId)
                .orElseGet(() -> {
                    String roomId = "telemed-APPT" + appointmentId;
                    VideoSession session = VideoSession.builder()
                            .appointmentId(appointmentId)
                            .roomId(roomId)
                            .jitsiUrl(JITSI_BASE_URL + roomId)
                            .startTime(Instant.now())
                            .status("ACTIVE")
                            .build();
                    return videoSessionRepository.save(session);
                });
    }

    public VideoSession getSessionByAppointmentId(String appointmentId) {
        return videoSessionRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found for this appointment"));
    }

    public VideoSession endSession(String appointmentId) {
        VideoSession session = getSessionByAppointmentId(appointmentId);
        session.setEndTime(Instant.now());
        session.setStatus("ENDED");
        return videoSessionRepository.save(session);
    }
}
