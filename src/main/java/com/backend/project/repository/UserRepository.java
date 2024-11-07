package com.backend.project.repository;

import com.backend.project.model.UserEntity;
import org.apache.catalina.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends MongoRepository<UserEntity, UUID> {

    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByMail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByMail(String mail);
}
