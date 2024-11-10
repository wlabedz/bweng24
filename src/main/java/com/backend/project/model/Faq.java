package com.backend.project.model;


import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.mongodb.core.mapping.Document;



@Document(collection = "Faq")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Faq {

    @Id
    private String id;
    private String Question;
    private String Answer;

    // Getters and Setters

    public Faq(String question, String answer) {
        this.Question = question;
        this.Answer = answer;
    }

//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }

//    public String getQuestion() {
//        return Question;
//    }
//
//    public void setQuestion(String question) {
//        this.Question = question;
//    }
//
//    public String getAnswer() {
//        return Answer;
//    }
//
//    public void setAnswer(String answer) {
//        this.Answer = answer;
//    }


}
