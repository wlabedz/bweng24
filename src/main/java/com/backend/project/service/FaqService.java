package com.backend.project.service;


import com.backend.project.dto.FaqDto;
import com.backend.project.model.Faq;
import com.backend.project.repository.FaqRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FaqService {

    private FaqRepository faqRepository;

    @Autowired
    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    public List<Faq> getAllFaqs(){
        return faqRepository.findAll();
    }

    public Faq getFaqById(String id){
        return faqRepository.findById(id).get();
    }

    public Faq addFaq(FaqDto faqDto){
        Faq faq = new Faq(faqDto.question(),faqDto.answer());
        return faqRepository.save(faq);
    }

}
