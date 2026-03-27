package com.healthcare.telemedicine.controller;

import com.healthcare.telemedicine.dto.DirectSessionRequest;
import com.healthcare.telemedicine.dto.ConsultationDto;
import com.healthcare.telemedicine.dto.CreateConsultationRequest;
import com.healthcare.telemedicine.dto.ScheduleConsultationRequest;
import com.healthcare.telemedicine.dto.JoinSessionResponse;
import com.healthcare.telemedicine.dto.VideoSessionDto;
import com.healthcare.telemedicine.model.Consultation;
import com.healthcare.telemedicine.model.VideoSession;
import com.healthcare.telemedicine.service.ConsultationService;
import com.healthcare.telemedicine.service.VideoSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@RestController
@RequestMapping("/api/telemedicine")
public class TelemedicineController {

    private final VideoSessionService videoSessionService;
    private final ConsultationService consultationService;

    public TelemedicineController(VideoSessionService videoSessionService,
                                  ConsultationService consultationService) {
        this.videoSessionService = videoSessionService;
        this.consultationService = consultationService;
    }

    @PostMapping("/session/{appointmentId}")
    public ResponseEntity<VideoSessionDto> createSession(@PathVariable String appointmentId) {
        VideoSession session = videoSessionService.createSession(appointmentId);
        return ResponseEntity.ok(mapToDto(session));
    }

    @PostMapping("/session/patient/{patientId}")
    public ResponseEntity<VideoSessionDto> createPatientSession(@PathVariable String patientId) {
        VideoSession session = videoSessionService.createPatientSession(patientId);
        return ResponseEntity.ok(mapToDto(session));
    }

    @PostMapping("/session/direct")
    public ResponseEntity<VideoSessionDto> createDirectSession(@RequestBody DirectSessionRequest request) {
        VideoSession session = videoSessionService.createDirectSession(request.getPatientId(), request.getPeerPatientId());
        return ResponseEntity.ok(mapToDto(session));
    }

    @GetMapping("/session/{appointmentId}/join")
    public ResponseEntity<JoinSessionResponse> joinSession(@PathVariable String appointmentId) {
        VideoSession session = videoSessionService.getSessionByAppointmentId(appointmentId);
        return ResponseEntity.ok(new JoinSessionResponse(session.getJitsiUrl()));
    }

    @PostMapping("/session/{appointmentId}/end")
    public ResponseEntity<VideoSessionDto> endSession(@PathVariable String appointmentId) {
        VideoSession session = videoSessionService.endSession(appointmentId);
        return ResponseEntity.ok(mapToDto(session));
    }

    @GetMapping("/session/{appointmentId}")
    public ResponseEntity<VideoSessionDto> getSession(@PathVariable String appointmentId) {
        VideoSession session = videoSessionService.getSessionByAppointmentId(appointmentId);
        return ResponseEntity.ok(mapToDto(session));
    }

    private VideoSessionDto mapToDto(VideoSession session) {
        return VideoSessionDto.builder()
                .appointmentId(session.getAppointmentId())
                .roomId(session.getRoomId())
                .jitsiUrl(session.getJitsiUrl())
                .status(session.getStatus())
                .startTime(session.getStartTime())
                .build();
    }

    @PostMapping("/consultations")
    public ResponseEntity<ConsultationDto> createConsultation(@RequestBody CreateConsultationRequest request,
                                                              Authentication authentication) {
        if (!hasDoctorRole(authentication)) {
            return ResponseEntity.status(403).build();
        }
        Consultation created = consultationService.createRingingConsultation(request.getDoctorId(), request.getPatientId());
        return ResponseEntity.ok(mapToDto(created));
    }

    @PostMapping("/consultations/schedule")
    public ResponseEntity<ConsultationDto> scheduleConsultation(@RequestBody ScheduleConsultationRequest request,
                                                                Authentication authentication) {
        if (!hasDoctorRole(authentication)) {
            return ResponseEntity.status(403).build();
        }
        Consultation created = consultationService.scheduleConsultation(request.getDoctorId(), request.getPatientId(), request.getScheduledAt());
        return ResponseEntity.ok(mapToDto(created));
    }

    @GetMapping("/consultations/patient/{patientId}")
    public ResponseEntity<List<ConsultationDto>> listForPatient(@PathVariable String patientId) {
        List<ConsultationDto> list = consultationService.listForPatient(patientId).stream().map(this::mapToDto).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/consultations/doctor/{doctorId}")
    public ResponseEntity<List<ConsultationDto>> listForDoctor(@PathVariable String doctorId) {
        List<ConsultationDto> list = consultationService.listForDoctor(doctorId).stream().map(this::mapToDto).toList();
        return ResponseEntity.ok(list);
    }

    private boolean hasDoctorRole(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) return false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority == null) continue;
            String role = authority.getAuthority();
            if (role == null) continue;
            String normalized = role.trim().toUpperCase();
            if (normalized.equals("DOCTOR") || normalized.equals("ROLE_DOCTOR")) {
                return true;
            }
        }
        return false;
    }

    @GetMapping("/consultations/{consultationId}")
    public ResponseEntity<ConsultationDto> getConsultation(@PathVariable String consultationId) {
        return ResponseEntity.ok(mapToDto(consultationService.get(consultationId)));
    }

    @PostMapping("/consultations/{consultationId}/answer")
    public ResponseEntity<ConsultationDto> answer(@PathVariable String consultationId) {
        Consultation answered = consultationService.answer(consultationId);
        return ResponseEntity.ok(mapToDto(answered));
    }

    @PostMapping("/consultations/{consultationId}/decline")
    public ResponseEntity<ConsultationDto> decline(@PathVariable String consultationId) {
        Consultation declined = consultationService.decline(consultationId);
        return ResponseEntity.ok(mapToDto(declined));
    }

    @PostMapping("/consultations/{consultationId}/end")
    public ResponseEntity<ConsultationDto> end(@PathVariable String consultationId) {
        Consultation ended = consultationService.end(consultationId);
        return ResponseEntity.ok(mapToDto(ended));
    }

    private ConsultationDto mapToDto(Consultation consultation) {
        return ConsultationDto.builder()
                .id(consultation.getId())
                .doctorId(consultation.getDoctorId())
                .patientId(consultation.getPatientId())
                .roomId(consultation.getRoomId())
                .jitsiUrl(consultation.getJitsiUrl())
                .status(consultation.getStatus())
                .scheduledAt(consultation.getScheduledAt())
                .createdAt(consultation.getCreatedAt())
                .answeredAt(consultation.getAnsweredAt())
                .endedAt(consultation.getEndedAt())
                .build();
    }
}
