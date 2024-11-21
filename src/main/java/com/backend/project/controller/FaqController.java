package com.backend.project.controller;


import com.backend.project.dto.FaqDto;
import com.backend.project.model.Faq;
import com.backend.project.service.FaqService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    @PostMapping
    public ResponseEntity<Faq> addFaq(@RequestBody @Valid FaqDto faqDto) {
        String id = faqService.addFaq(faqDto).getId().toString();
        return ResponseEntity
                .created(URI.create("/faqs/" + id))
                .build();
    }


}
