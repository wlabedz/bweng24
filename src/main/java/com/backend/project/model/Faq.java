package com.backend.project.model;


import jakarta.persistence.Id;

import lombok.Data;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;


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

    private boolean Approved = false; // Default value is false

    // Constructor
    public Faq(String Question, String Answer, Boolean Approved) {
        this.Question = Question;
        this.Answer = Answer;
        this.Approved = Approved;
    }


}
