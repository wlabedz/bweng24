package com.backend.project.controller;


import com.backend.project.dto.FaqDto;
import com.backend.project.model.Faq;
import com.backend.project.service.FaqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController //REST API endpoints.
@RequestMapping("/api/faqs")
public class FaqController {


    private final FaqService faqService;

    // Constructor injection for the service
    @Autowired
    public FaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    //Endpoint
    @GetMapping
    public ResponseEntity<List<Faq>> getAllFaqs() {
        List<Faq> faqs = faqService.getAllFaqs();
        return ResponseEntity.ok(faqs);
    }

    // --- Users ---
    @PostMapping("/ask")
    public ResponseEntity<String> askQuestion(@RequestBody FaqDto faqDto){
        faqService.saveFaq(faqDto);
        return ResponseEntity.ok("Question Submitted successfully!\n Wait for our staff to provide an answer\n");
    }


    //-- Admins --
    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approveQuestion(@PathVariable String id){
        faqService.approveFaq(id);
        return ResponseEntity.ok("Question Approved successfully!");
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<String> deleteFaq(@PathVariable String id){
        try {
            faqService.deleteFaq(id);
            return ResponseEntity.ok("Faq Deleted Successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("FAQ not found");
        }
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<String> updateFaq(@PathVariable String id, @RequestBody FaqDto faqDto){
        try {
            faqService.updateFaq(id, faqDto);
            return ResponseEntity.ok("Faq Updated Successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("FAQ not found");
        }

    }
}
