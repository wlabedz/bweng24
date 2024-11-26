package com.backend.project.repository;


import com.backend.project.model.Faq;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FaqRepository extends MongoRepository<Faq, String> {
}
