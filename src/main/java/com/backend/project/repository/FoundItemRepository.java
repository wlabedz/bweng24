package com.backend.project.repository;

import com.backend.project.model.FoundItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;


public interface FoundItemRepository extends MongoRepository<FoundItem, UUID> {}