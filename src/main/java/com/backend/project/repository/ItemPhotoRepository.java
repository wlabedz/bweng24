package com.backend.project.repository;

import com.backend.project.model.PhotoItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ItemPhotoRepository extends MongoRepository<PhotoItem, UUID> {
}
