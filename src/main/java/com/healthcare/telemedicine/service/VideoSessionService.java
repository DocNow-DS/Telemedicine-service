package com.healthcare.telemedicine.service;

import com.healthcare.telemedicine.dto.AppointmentDto;
import com.healthcare.telemedicine.model.VideoSession;
import com.healthcare.telemedicine.model.UserProjection;
import com.healthcare.telemedicine.repository.UserProjectionRepository;
import com.healthcare.telemedicine.repository.VideoSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
public class VideoSessionService {

    private final VideoSessionRepository videoSessionRepository;
    private final UserProjectionRepository userProjectionRepository;
    private final AppointmentServiceClient appointmentServiceClient;
    private final String vpaasBaseUrl;
    private final String vpaasMagicCookie;
    private final boolean appointmentValidationEnabled;
    private final boolean patientExistenceCheckEnabled;

    public VideoSessionService(VideoSessionRepository videoSessionRepository,
                               UserProjectionRepository userProjectionRepository,
                               AppointmentServiceClient appointmentServiceClient,
                               @Value("${vpaas.base-url}") String vpaasBaseUrl,
                               @Value("${vpaas.magic-cookie}") String vpaasMagicCookie,
                               @Value("${telemedicine.appointment-validation.enabled:false}") boolean appointmentValidationEnabled,
                               @Value("${telemedicine.patient-id-check.enabled:false}") boolean patientExistenceCheckEnabled) {
        this.videoSessionRepository = videoSessionRepository;
        this.userProjectionRepository = userProjectionRepository;
        this.appointmentServiceClient = appointmentServiceClient;
        this.vpaasBaseUrl = normalize(vpaasBaseUrl);
        this.vpaasMagicCookie = normalize(vpaasMagicCookie);
        this.appointmentValidationEnabled = appointmentValidationEnabled;
        this.patientExistenceCheckEnabled = patientExistenceCheckEnabled;
    }

    public VideoSession createSession(String appointmentId) {
        String normalizedAppointmentId = sanitizeSessionId(appointmentId);
        if (normalizedAppointmentId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "appointmentId is required");
        }

        if (appointmentValidationEnabled) {
            // 1. Fetch appointment details from Appointment Service
            AppointmentDto appointment = appointmentServiceClient.getAppointment(normalizedAppointmentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

            // 2. Validate status
            if (!"CONFIRMED".equalsIgnoreCase(appointment.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Appointment is not confirmed");
            }
        }

        // 3. Check if session already exists
        return findOrCreateSession(normalizedAppointmentId, "telemed-APPT-" + normalizedAppointmentId);
    }

    public VideoSession createPatientSession(String patientId) {
        String normalizedPatientId = sanitizeParticipantId(patientId);
        if (normalizedPatientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientId is required");
        }

        verifyPatientId(normalizedPatientId);
        String sessionKey = "patient-" + normalizedPatientId;

        return findOrCreateSession(sessionKey, "telemed-PATIENT-" + normalizedPatientId);
    }

    public VideoSession createDirectSession(String patientId, String peerPatientId) {
        String left = sanitizeParticipantId(patientId);
        String right = sanitizeParticipantId(peerPatientId);

        if (left.isBlank() || right.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both patientId and peerPatientId are required");
        }
        if (left.equals(right)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientId and peerPatientId must be different");
        }

        verifyPatientId(left);
        verifyPatientId(right);

        String first = left.compareTo(right) <= 0 ? left : right;
        String second = left.compareTo(right) <= 0 ? right : left;
        String sessionKey = "direct-" + first + "-" + second;

        return findOrCreateSession(sessionKey, "telemed-DIRECT-" + first + "-" + second);
    }

    public VideoSession getSessionByAppointmentId(String appointmentId) {
        try {
            return videoSessionRepository.findByAppointmentId(appointmentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found for this appointment"));
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Telemedicine datastore unavailable");
        }
    }

    public VideoSession endSession(String appointmentId) {
        VideoSession session = getSessionByAppointmentId(appointmentId);
        session.setEndTime(Instant.now());
        session.setStatus("ENDED");
        try {
            return videoSessionRepository.save(session);
        } catch (Exception ex) {
            return session;
        }
    }

    private VideoSession findOrCreateSession(String key, String roomId) {
        try {
            Optional<VideoSession> existing = videoSessionRepository.findByAppointmentId(key);
            if (existing.isPresent()) return existing.get();

            VideoSession fresh = buildActiveSession(key, roomId);
            try {
                return videoSessionRepository.save(fresh);
            } catch (Exception ignored) {
                // Fallback to non-persistent session so calls still start when DB is down.
                return fresh;
            }
        } catch (Exception ignored) {
            return buildActiveSession(key, roomId);
        }
    }

    private VideoSession buildActiveSession(String key, String roomId) {
        return VideoSession.builder()
                .appointmentId(key)
                .roomId(roomId)
                .jitsiUrl(buildJitsiUrl(roomId))
                .startTime(Instant.now())
                .status("ACTIVE")
                .build();
    }

    private String buildJitsiUrl(String roomId) {
        return vpaasBaseUrl + "/" + vpaasMagicCookie + "/" + roomId;
    }

    private static String normalize(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        return trimmed;
    }

    private static String sanitizeParticipantId(String raw) {
        if (raw == null) return "";
        return raw.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "-");
    }

    private static String sanitizeSessionId(String raw) {
        if (raw == null) return "";
        return raw.trim().replaceAll("[^a-zA-Z0-9_-]", "-");
    }

    private void verifyPatientId(String patientId) {
        if (patientId.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid patientId format");
        }

        if (!patientExistenceCheckEnabled) {
            return;
        }

        UserProjection projection = userProjectionRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found: " + patientId));

        if (!projection.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient is disabled: " + patientId);
        }
    }
}
