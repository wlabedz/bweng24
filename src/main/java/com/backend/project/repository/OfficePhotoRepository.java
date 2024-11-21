package com.backend.project.repository;

import com.backend.project.model.Office;
import com.backend.project.model.OfficePhoto;
import com.backend.project.model.UserPhoto;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface OfficePhotoRepository extends MongoRepository<OfficePhoto, UUID> {
}
