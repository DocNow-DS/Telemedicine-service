package com.healthcare.telemedicine.repository;

import com.healthcare.telemedicine.model.VideoSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VideoSessionRepository extends MongoRepository<VideoSession, String> {
    Optional<VideoSession> findByAppointmentId(String appointmentId);
}
