package com.healthcare.telemedicine.repository;

import com.healthcare.telemedicine.model.UserProjection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProjectionRepository extends MongoRepository<UserProjection, String> {
}
