package com.healthcare.telemedicine.service;

import com.healthcare.telemedicine.model.Consultation;
import com.healthcare.telemedicine.model.VideoSession;
import com.healthcare.telemedicine.model.UserProjection;
import com.healthcare.telemedicine.repository.ConsultationRepository;
import com.healthcare.telemedicine.repository.UserProjectionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final VideoSessionService videoSessionService;
    private final UserProjectionRepository userProjectionRepository;

    public ConsultationService(ConsultationRepository consultationRepository,
                              VideoSessionService videoSessionService,
                              UserProjectionRepository userProjectionRepository) {
        this.consultationRepository = consultationRepository;
        this.videoSessionService = videoSessionService;
        this.userProjectionRepository = userProjectionRepository;
    }

    public Consultation createRingingConsultation(String doctorId, String patientId) {
        String normalizedDoctorId = sanitizeId(doctorId);
        String normalizedPatientId = sanitizeId(patientId);

        if (normalizedDoctorId.isBlank() || normalizedPatientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctorId and patientId are required");
        }
        if (normalizedDoctorId.equals(normalizedPatientId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctorId and patientId must be different");
        }

        assertUserExists(normalizedDoctorId, "doctorId");
        assertUserExists(normalizedPatientId, "patientId");

        // Use existing session generator to produce a stable room + jitsiUrl
        VideoSession session = videoSessionService.createDirectSession(normalizedDoctorId, normalizedPatientId);

        Consultation consultation = new Consultation();
        consultation.setDoctorId(normalizedDoctorId);
        consultation.setPatientId(normalizedPatientId);
        consultation.setSessionKey(session.getAppointmentId());
        consultation.setRoomId(session.getRoomId());
        consultation.setJitsiUrl(session.getJitsiUrl());
        consultation.setStatus("RINGING");
        consultation.setCreatedAt(Instant.now());
        return consultationRepository.save(consultation);
    }

    public Consultation scheduleConsultation(String doctorId, String patientId, String scheduledAtIso) {
        String normalizedDoctorId = sanitizeId(doctorId);
        String normalizedPatientId = sanitizeId(patientId);

        if (normalizedDoctorId.isBlank() || normalizedPatientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctorId and patientId are required");
        }

        Instant scheduledAt;
        try {
            scheduledAt = Instant.parse(scheduledAtIso);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scheduledAt must be ISO-8601 instant (e.g. 2026-03-27T05:00:00Z)");
        }

        assertUserExists(normalizedDoctorId, "doctorId");
        assertUserExists(normalizedPatientId, "patientId");

        VideoSession session = videoSessionService.createDirectSession(normalizedDoctorId, normalizedPatientId);

        Consultation consultation = new Consultation();
        consultation.setDoctorId(normalizedDoctorId);
        consultation.setPatientId(normalizedPatientId);
        consultation.setSessionKey(session.getAppointmentId());
        consultation.setRoomId(session.getRoomId());
        consultation.setJitsiUrl(session.getJitsiUrl());
        consultation.setStatus("SCHEDULED");
        consultation.setScheduledAt(scheduledAt);
        consultation.setCreatedAt(Instant.now());
        return consultationRepository.save(consultation);
    }

    public List<Consultation> listForPatient(String patientId) {
        String normalizedPatientId = sanitizeId(patientId);
        if (normalizedPatientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientId is required");
        }
        return consultationRepository.findByPatientIdOrderByCreatedAtDesc(normalizedPatientId);
    }

    public List<Consultation> listForDoctor(String doctorId) {
        String normalizedDoctorId = sanitizeId(doctorId);
        if (normalizedDoctorId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctorId is required");
        }
        return consultationRepository.findByDoctorIdOrderByCreatedAtDesc(normalizedDoctorId);
    }

    public Consultation answer(String consultationId) {
        Consultation consultation = get(consultationId);
        if ("ENDED".equalsIgnoreCase(consultation.getStatus())) {
            return consultation;
        }
        consultation.setStatus("ACTIVE");
        consultation.setAnsweredAt(Instant.now());
        return consultationRepository.save(consultation);
    }

    public Consultation decline(String consultationId) {
        Consultation consultation = get(consultationId);
        if ("ENDED".equalsIgnoreCase(consultation.getStatus())) {
            return consultation;
        }
        consultation.setStatus("DECLINED");
        consultation.setEndedAt(Instant.now());
        return consultationRepository.save(consultation);
    }

    public Consultation end(String consultationId) {
        Consultation consultation = get(consultationId);
        consultation.setStatus("ENDED");
        consultation.setEndedAt(Instant.now());
        return consultationRepository.save(consultation);
    }

    public Consultation get(String consultationId) {
        return consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consultation not found"));
    }

    private void assertUserExists(String id, String fieldName) {
        UserProjection user = userProjectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, fieldName + " not found"));
        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, fieldName + " is disabled");
        }
    }

    private String sanitizeId(String raw) {
        if (raw == null) return "";
        return raw.trim();
    }
}
