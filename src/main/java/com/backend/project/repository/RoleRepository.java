package com.backend.project.repository;

import com.backend.project.model.Roles;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends MongoRepository<Roles,UUID> {

    Optional<Roles> findByName(String name);

    int deleteByName(String name);

}
