package com.backend.project.model;


import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

//  MongoDB document.
@Document(collection = "Faq")
@Data
public class Faq {

    @Id
    private String id;

    private String Question;
    private String Answer;


    private LocalDate createdAt;
    private LocalDateTime updatedAt;

    private boolean Approved = false; // Default value is false

    // Constructor
    public Faq(String Question, String Answer, Boolean Approved) {
        this.Question = Question;
        this.Answer = Answer;
        this.createdAt = LocalDate.now();
        this.Approved = Approved != null ? Approved : false; // Default to false if null
    }

}