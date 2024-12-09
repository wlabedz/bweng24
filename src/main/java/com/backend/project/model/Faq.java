package com.backend.project.model;


import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

//  MongoDB document.
@Document(collection = "Faq")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Faq {

    @Id
    private String id;

    private String question;
    private String answer;


    private LocalDate createdAt;
    private LocalDateTime updatedAt;

    private boolean approved = false;

    // Constructor for creating new FAQs
    public Faq(String question, String answer, boolean approved) {
        this.question = question;
        this.answer = answer;
        this.createdAt = LocalDate.now();
        this.approved = approved;
    }

}