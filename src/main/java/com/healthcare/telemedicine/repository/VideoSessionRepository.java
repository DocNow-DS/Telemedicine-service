package com.healthcare.telemedicine.repository;

import com.healthcare.telemedicine.model.VideoSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface VideoSessionRepository extends MongoRepository<VideoSession, String> {
    Optional<VideoSession> findByAppointmentId(String appointmentId);
    Optional<VideoSession> findTopByAppointmentIdOrderByStartTimeDesc(String appointmentId);
    Optional<VideoSession> findTopByAppointmentIdAndStatusNotOrderByStartTimeDesc(String appointmentId, String status);
    List<VideoSession> findByDoctorIdOrderByStartTimeDesc(String doctorId);
    List<VideoSession> findByPatientIdOrderByStartTimeDesc(String patientId);
}
