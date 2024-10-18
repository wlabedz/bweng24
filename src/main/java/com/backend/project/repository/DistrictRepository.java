package com.backend.project.repository;

import com.backend.project.model.District;
import com.backend.project.model.Office;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface DistrictRepository extends MongoRepository<District, Integer> {
}
