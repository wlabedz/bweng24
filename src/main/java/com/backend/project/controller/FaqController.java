package com.backend.project.controller;


import com.backend.project.model.Faq;
import com.backend.project.service.FaqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
public class FaqController {

    private final FaqService faqService;

    @Autowired
    public FaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    //Endpoint to get all questions from the FAQ
    @GetMapping
    public List<Faq> getAllFaqs(){
        return faqService.getAllFaqs();
    }

}
