package com.healthcare.telemedicine.service;

import com.healthcare.telemedicine.dto.AppointmentDto;
import com.healthcare.telemedicine.model.VideoSession;
import com.healthcare.telemedicine.model.UserProjection;
import com.healthcare.telemedicine.repository.UserProjectionRepository;
import com.healthcare.telemedicine.repository.VideoSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class VideoSessionService {

    private static final Logger logger = LoggerFactory.getLogger(VideoSessionService.class);

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
        return createOrGetAppointmentSession(appointmentId, null, null, false);
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
        String normalizedAppointmentId = sanitizeSessionId(appointmentId);
        try {
            return videoSessionRepository.findTopByAppointmentIdOrderByStartTimeDesc(normalizedAppointmentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found for this appointment"));
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Telemedicine datastore unavailable");
        }
    }

    public VideoSession endSession(String appointmentId) {
        return endSessionForAppointment(appointmentId, "system", null);
    }

    public VideoSession createOrGetAppointmentSession(String appointmentId,
                                                      String patientId,
                                                      String doctorId,
                                                      boolean forceNew) {
        String normalizedAppointmentId = sanitizeSessionId(appointmentId);
        if (normalizedAppointmentId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "appointmentId is required");
        }

        String normalizedPatientId = sanitizeParticipantId(patientId);
        String normalizedDoctorId = sanitizeParticipantId(doctorId);

        if (appointmentValidationEnabled) {
            AppointmentDto appointment = appointmentServiceClient.getAppointment(normalizedAppointmentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
            if (!"CONFIRMED".equalsIgnoreCase(appointment.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Appointment is not confirmed");
            }
        }

        try {
            if (forceNew) {
                videoSessionRepository
                        .findTopByAppointmentIdAndStatusNotOrderByStartTimeDesc(normalizedAppointmentId, "ENDED")
                        .ifPresent(existing -> endSessionForAppointment(normalizedAppointmentId, normalizedDoctorId, "Force new session"));
            } else {
                Optional<VideoSession> active = videoSessionRepository
                        .findTopByAppointmentIdAndStatusNotOrderByStartTimeDesc(normalizedAppointmentId, "ENDED");
                if (active.isPresent()) return active.get();
            }
        } catch (Exception ex) {
            logger.warn("Datastore read failed while creating telemed session for appointment {}. Falling back to non-persistent session.", normalizedAppointmentId, ex);
            String fallbackRoomId = buildRoomId(normalizedAppointmentId);
            return buildActiveSession(normalizedAppointmentId, fallbackRoomId, normalizedPatientId, normalizedDoctorId);
        }

        String roomId = buildRoomId(normalizedAppointmentId);
        VideoSession fresh = buildActiveSession(normalizedAppointmentId, roomId, normalizedPatientId, normalizedDoctorId);

        try {
            return videoSessionRepository.save(fresh);
        } catch (Exception ignored) {
            return fresh;
        }
    }

    public VideoSession getLatestSessionForAppointment(String appointmentId) {
        return getSessionByAppointmentId(appointmentId);
    }

    public VideoSession endSessionForAppointment(String appointmentId, String endedBy, String notes) {
        String normalizedAppointmentId = sanitizeSessionId(appointmentId);
        VideoSession session = videoSessionRepository
                .findTopByAppointmentIdAndStatusNotOrderByStartTimeDesc(normalizedAppointmentId, "ENDED")
                .orElseGet(() -> getSessionByAppointmentId(normalizedAppointmentId));

        Instant now = Instant.now();
        session.setEndTime(now);
        session.setStatus("ENDED");
        session.setEndedBy(sanitizeParticipantId(endedBy));
        session.setNotes(notes);
        session.setUpdatedAt(now);

        try {
            return videoSessionRepository.save(session);
        } catch (Exception ex) {
            return session;
        }
    }

    public List<VideoSession> listSessionsForDoctor(String doctorId) {
        String normalizedDoctorId = sanitizeParticipantId(doctorId);
        if (normalizedDoctorId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctorId is required");
        }
        try {
            return videoSessionRepository.findByDoctorIdOrderByStartTimeDesc(normalizedDoctorId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Telemedicine datastore unavailable");
        }
    }

    public List<VideoSession> listSessionsForPatient(String patientId) {
        String normalizedPatientId = sanitizeParticipantId(patientId);
        if (normalizedPatientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientId is required");
        }
        try {
            return videoSessionRepository.findByPatientIdOrderByStartTimeDesc(normalizedPatientId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Telemedicine datastore unavailable");
        }
    }

    private VideoSession findOrCreateSession(String key, String roomId) {
        try {
            Optional<VideoSession> existing = videoSessionRepository.findTopByAppointmentIdAndStatusNotOrderByStartTimeDesc(key, "ENDED");
            if (existing.isPresent()) return existing.get();

            VideoSession fresh = buildActiveSession(key, roomId, null, null);
            try {
                return videoSessionRepository.save(fresh);
            } catch (Exception ignored) {
                // Fallback to non-persistent session so calls still start when DB is down.
                return fresh;
            }
        } catch (Exception ignored) {
            return buildActiveSession(key, roomId, null, null);
        }
    }

    private VideoSession buildActiveSession(String key, String roomId, String patientId, String doctorId) {
        Instant now = Instant.now();
        return VideoSession.builder()
                .appointmentId(key)
                .patientId(patientId)
                .doctorId(doctorId)
                .roomId(roomId)
                .jitsiUrl(buildJitsiUrl(roomId))
                .startTime(now)
                .createdAt(now)
                .updatedAt(now)
                .status("IN_SESSION")
                .build();
    }

    private String buildJitsiUrl(String roomId) {
        if (vpaasMagicCookie == null || vpaasMagicCookie.isBlank()) {
            return vpaasBaseUrl + "/" + roomId;
        }
        return vpaasBaseUrl + "/" + vpaasMagicCookie + "/" + roomId;
    }

    private String buildRoomId(String appointmentId) {
        String normalized = sanitizeParticipantId(appointmentId);
        if (normalized.isBlank()) {
            normalized = "session";
        }
        // Keep room names short and unique to avoid stale Jitsi room settings.
        if (normalized.length() > 28) {
            normalized = normalized.substring(0, 28);
        }
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "telemed-" + normalized + "-" + suffix;
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
