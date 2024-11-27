package com.backend.project.service;


import com.backend.project.dto.FaqDto;
import com.backend.project.model.Faq;
import com.backend.project.repository.FaqRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// service and managed by Spring.
@Service
public class FaqService {

    private FaqRepository faqRepository;

    @Autowired
    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    // Method to fetch all FAQ entries
    public List<Faq> getAllFaqs(){
        return faqRepository.findAll();
    }

    // --User--
    // Save FAQ ( user-submission)
    public void saveFaq(Faq faq){
        faq.setApproved(false);
        faqRepository.save(faq);
    }

    // --Admin ---
    public void approveFaq(String id){
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() ->  new RuntimeException("Faq not found"));
        faq.setApproved(true);
        faqRepository.save(faq);
    }

    public void updateFaq(String id, Faq Updatedfaq){
        faqRepository.findById(id)
                .map(faq -> {
                    faq.setQuestion(Updatedfaq.getQuestion());
                    faq.setAnswer(Updatedfaq.getAnswer());
                    return faqRepository.save(faq);
                })
                .orElseThrow(() -> new RuntimeException("Faq not found" + id));
    }

    public void deleteFaq(String id) {
        if (!faqRepository.existsById(id)) {
            throw new RuntimeException("Faq not found: " + id);
        }
        faqRepository.deleteById(id);
    }



}
