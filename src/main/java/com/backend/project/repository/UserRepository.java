package com.backend.project.repository;

import com.backend.project.model.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends MongoRepository<UserEntity, UUID> {

    Optional<UserEntity> findByUsername(String username);

    Boolean existsByUsername(String username);
}
