package com.backend.project.repository;

import com.backend.project.model.Office;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface OfficeRepository extends MongoRepository<Office, UUID> {
}
