package com.backend.project.repository;

import com.backend.project.model.PhotoOffice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface OfficePhotoRepository extends MongoRepository<PhotoOffice, UUID> {
}
