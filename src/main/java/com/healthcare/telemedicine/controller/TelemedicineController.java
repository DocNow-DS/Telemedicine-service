package com.healthcare.telemedicine.controller;

import com.healthcare.telemedicine.dto.JoinSessionResponse;
import com.healthcare.telemedicine.dto.VideoSessionDto;
import com.healthcare.telemedicine.model.VideoSession;
import com.healthcare.telemedicine.service.VideoSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/telemedicine")
@RequiredArgsConstructor
public class TelemedicineController {

    private final VideoSessionService videoSessionService;

    @PostMapping("/session/{appointmentId}")
    public ResponseEntity<VideoSessionDto> createSession(@PathVariable String appointmentId) {
        VideoSession session = videoSessionService.createSession(appointmentId);
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
}
