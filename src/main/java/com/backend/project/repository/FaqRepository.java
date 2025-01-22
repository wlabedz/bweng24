package com.backend.project.repository;


import com.backend.project.model.Faq;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface FaqRepository extends MongoRepository<Faq, String> {
    Iterable<? extends Faq> findByQuestionContaining(String s);
    Optional<Faq> findByQuestion(String s);
}
