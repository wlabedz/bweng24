package com.backend.project.model;


import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.mongodb.core.mapping.Document;



import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;


//  MongoDB document.
@Document(collection = "Faq")
@Data
public class Faq {

    @Getter
    @Id
    private String id;
    @Getter
    @Setter
    private String Question;
    @Setter
    @Getter
    private String Answer;

    @Setter
    @Getter
    private LocalDate createdAt;

    @Setter
    @Getter
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