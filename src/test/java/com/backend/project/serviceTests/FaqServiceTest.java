package com.backend.project.serviceTests;

import com.backend.project.dto.FaqDto;
import com.backend.project.model.District;
import com.backend.project.model.Faq;
import com.backend.project.model.Office;
import com.backend.project.repository.FaqRepository;
import com.backend.project.service.FaqService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class FaqServiceTest {

    private FaqService faqService;

    @Mock
    private FaqRepository faqRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        faqService = new FaqService(faqRepository);
    }

    @Test
    void saveFaq_WhenFaqDtoIsValid_CallsSaveMethod() {
        FaqDto faqDto = new FaqDto("Where are offices located?", "Our offices are located in different districts of Vienna.");

        faqService.saveFaq(faqDto);

        verify(faqRepository, times(1)).save(any(Faq.class));
    }

    @Test
    void approveFaq_WhenFaqExists_ApprovesFaq() {
        String faqId = "123";
        Faq faq = new Faq("What are the opening hours on Saturday?", "Our offices are open from 8 to 12 on Saturdays.", false);
        when(faqRepository.findById(faqId)).thenReturn(Optional.of(faq));

        faqService.approveFaq(faqId);

        verify(faqRepository, times(1)).save(faq);
        assert(faq.isApproved());
    }

    @Test
    void updateFaq_WhenFaqExists_UpdatesFaq() {
        String faqId = "1";
        FaqDto faqDto = new FaqDto("Updated Question", "Updated Answer");
        Faq faq = new Faq("Old Question", "Old Answer", false);
        when(faqRepository.findById(faqId)).thenReturn(Optional.of(faq));

        faqService.updateFaq(faqId, faqDto);

        assertEquals("Updated Question", faq.getQuestion());
        assertEquals("Updated Answer", faq.getAnswer());
        verify(faqRepository, times(1)).save(faq);
    }

    @Test
    void deleteFaq_WhenFaqExists_DeletesFaq() {
        String faqId = "1";
        when(faqRepository.existsById(faqId)).thenReturn(true);
        doNothing().when(faqRepository).deleteById(faqId);

        faqService.deleteFaq(faqId);

        verify(faqRepository, times(1)).deleteById(faqId);
    }

    @Test
    void deleteFaq_WhenFaqDoesNotExist_ThrowsException() {
        String faqId = "1";
        when(faqRepository.existsById(faqId)).thenReturn(false);

        assertThrows(RuntimeException.class,()->faqService.deleteFaq(faqId));
    }

    @Test
    void getAllFaqs_WhenFaqsExist_ReturnsFaqList(){
        List<Faq> faqs = Arrays.asList(
                new Faq("Question1","Answer1",true),
                new Faq("Question2","Answer2",false)
        );

        when(faqRepository.findAll()).thenReturn(faqs);

        List<Faq> result = faqService.getAllFaqs();

        assertEquals(2, result.size());
        verify(faqRepository, times(1)).findAll();
    }
}
