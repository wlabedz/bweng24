package com.backend.project.repository;

import com.backend.project.model.ItemPhoto;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ItemPhotoRepository extends MongoRepository<ItemPhoto, UUID> {
}
