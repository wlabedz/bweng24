package com.backend.project.service;


import com.backend.project.dto.FaqDto;
import com.backend.project.model.Faq;
import com.backend.project.repository.FaqRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import java.time.LocalDateTime;
import java.util.List;

// service and managed by Spring.
@Service
public class FaqService {

    private final FaqRepository faqRepository;

    @Autowired
    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    // Fetch all FAQ entries
    public List<Faq> getAllFaqs(){
        return faqRepository.findAll();
    }

    // --User--
    public void saveFaq(FaqDto faqDto) {
        Faq faq = new Faq(
                faqDto.question(),
                faqDto.answer(),
                false
        );
        faqRepository.save(faq);
    }

    // --Admin ---
    public void approveFaq(String id){
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() ->  new RuntimeException("Faq not found"));
        faq.setApproved(true);
        faqRepository.save(faq);
    }

    public void updateFaq(String id, FaqDto faqDto){
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found"));
        faq.setQuestion(faqDto.question());
        faq.setAnswer(faqDto.answer());
        faq.setUpdatedAt(LocalDateTime.now());
        faqRepository.save(faq);
    }

    public void deleteFaq(String id) {
        if (!faqRepository.existsById(id)) {
            throw new RuntimeException("Faq not found: " + id);
        }
        faqRepository.deleteById(id);
    }



}
