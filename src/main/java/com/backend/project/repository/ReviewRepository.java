package com.backend.project.repository;

import com.backend.project.model.Review;
import com.backend.project.model.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends MongoRepository<Review, UUID> {
}
