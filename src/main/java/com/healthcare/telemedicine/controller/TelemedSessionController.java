package com.healthcare.telemedicine.controller;

import com.healthcare.telemedicine.dto.CreateSessionRequest;
import com.healthcare.telemedicine.dto.EndSessionRequest;
import com.healthcare.telemedicine.dto.SessionResponse;
import com.healthcare.telemedicine.model.VideoSession;
import com.healthcare.telemedicine.service.VideoSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/telemed")
public class TelemedSessionController {

    private final VideoSessionService videoSessionService;

    public TelemedSessionController(VideoSessionService videoSessionService) {
        this.videoSessionService = videoSessionService;
    }

    @PostMapping("/appointments/{appointmentId}/session")
    public ResponseEntity<SessionResponse> createOrGetSession(@PathVariable String appointmentId,
                                                              @RequestBody(required = false) CreateSessionRequest request,
                                                              Authentication authentication) {
        if (!hasAnyRole(authentication, "PATIENT", "DOCTOR", "ADMIN")) {
            return ResponseEntity.status(403).build();
        }

        String patientId = request != null ? request.getPatientId() : null;
        String doctorId = request != null ? request.getDoctorId() : null;
        boolean forceNew = request != null && Boolean.TRUE.equals(request.getForceNew());

        VideoSession session = videoSessionService.createOrGetAppointmentSession(appointmentId, patientId, doctorId, forceNew);
        return ResponseEntity.ok(mapToResponse(session));
    }

    @GetMapping("/appointments/{appointmentId}/session")
    public ResponseEntity<SessionResponse> getLatestSession(@PathVariable String appointmentId,
                                                            Authentication authentication) {
        if (!hasAnyRole(authentication, "PATIENT", "DOCTOR", "ADMIN")) {
            return ResponseEntity.status(403).build();
        }

        VideoSession session = videoSessionService.getLatestSessionForAppointment(appointmentId);
        return ResponseEntity.ok(mapToResponse(session));
    }

    @PostMapping("/appointments/{appointmentId}/end")
    public ResponseEntity<SessionResponse> endSession(@PathVariable String appointmentId,
                                                      @RequestBody(required = false) EndSessionRequest request,
                                                      Authentication authentication) {
        if (!hasAnyRole(authentication, "DOCTOR", "ADMIN")) {
            return ResponseEntity.status(403).build();
        }

        String endedBy = request != null ? request.getEndedBy() : null;
        String notes = request != null ? request.getNotes() : null;

        VideoSession ended = videoSessionService.endSessionForAppointment(appointmentId, endedBy, notes);
        return ResponseEntity.ok(mapToResponse(ended));
    }

    @GetMapping("/doctor/{doctorId}/sessions")
    public ResponseEntity<List<SessionResponse>> listDoctorSessions(@PathVariable String doctorId,
                                                                    Authentication authentication) {
        if (!hasAnyRole(authentication, "DOCTOR", "ADMIN")) {
            return ResponseEntity.status(403).build();
        }

        List<SessionResponse> list = videoSessionService.listSessionsForDoctor(doctorId)
                .stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/patient/{patientId}/sessions")
    public ResponseEntity<List<SessionResponse>> listPatientSessions(@PathVariable String patientId,
                                                                     Authentication authentication) {
        if (!hasAnyRole(authentication, "PATIENT", "DOCTOR", "ADMIN")) {
            return ResponseEntity.status(403).build();
        }

        List<SessionResponse> list = videoSessionService.listSessionsForPatient(patientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    private SessionResponse mapToResponse(VideoSession session) {
        return SessionResponse.builder()
                .id(session.getId())
                .appointmentId(session.getAppointmentId())
                .patientId(session.getPatientId())
                .doctorId(session.getDoctorId())
                .roomName(session.getRoomId())
                .sessionUrl(session.getJitsiUrl())
                .status(session.getStatus())
                .startedAt(session.getStartTime())
                .endedAt(session.getEndTime())
                .endedBy(session.getEndedBy())
                .notes(session.getNotes())
                .scheduledAt(session.getScheduledAt())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    private boolean hasAnyRole(Authentication authentication, String... roles) {
        if (authentication == null || authentication.getAuthorities() == null || roles == null) return false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority == null || authority.getAuthority() == null) continue;
            String value = authority.getAuthority().trim().toUpperCase();
            for (String role : roles) {
                if (role == null) continue;
                String normalized = role.trim().toUpperCase();
                if (value.equals(normalized) || value.equals("ROLE_" + normalized)) {
                    return true;
                }
            }
        }
        return false;
    }
}
