package com.backend.project.repository;

import com.backend.project.model.PhotoUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface UserPhotoRepository extends MongoRepository<PhotoUser, UUID> {
}
