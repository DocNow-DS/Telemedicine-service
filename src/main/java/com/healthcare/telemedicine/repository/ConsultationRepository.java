package com.healthcare.telemedicine.repository;

import com.healthcare.telemedicine.model.Consultation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConsultationRepository extends MongoRepository<Consultation, String> {
    List<Consultation> findByPatientIdOrderByCreatedAtDesc(String patientId);

    List<Consultation> findByDoctorIdOrderByCreatedAtDesc(String doctorId);
}
