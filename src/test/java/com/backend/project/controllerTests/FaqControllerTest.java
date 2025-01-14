package com.backend.project.controllerTests;

import com.backend.project.controller.FaqController;
import com.backend.project.dto.FaqDto;
import com.backend.project.model.Faq;
import com.backend.project.service.FaqService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FaqControllerTest {

    @InjectMocks
    private FaqController faqController;

    @Mock
    private FaqService faqService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllFaqs_WhenCalled_ReturnsAllFaqs() {
        Faq faq = new Faq("What is Spring?", "Spring is a framework", false);
        when(faqService.getAllFaqs()).thenReturn(Arrays.asList(faq));

        ResponseEntity<?> response = faqController.getAllFaqs();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("What is Spring?", ((Faq) ((Iterable<?>) response.getBody()).iterator().next()).getQuestion());
    }

    @Test
    void askQuestion_WithValidData_SubmitsQuestionSuccessfully() {
        FaqDto faqDto = new FaqDto("What is Java?", "Java is a programming language");
        doNothing().when(faqService).saveFaq(faqDto);

        ResponseEntity<String> response = faqController.askQuestion(faqDto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Question Submitted successfully!\n Wait for our staff to provide an answer\n", response.getBody());
    }

    @Test
    void approveQuestion_WithValidId_ApprovesQuestionSuccessfully() {
        String faqId = "1";
        doNothing().when(faqService).approveFaq(faqId);

        ResponseEntity<String> response = faqController.approveQuestion(faqId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Question Approved successfully!", response.getBody());
    }

    @Test
    void deleteFaq_WithValidId_DeletesFaqSuccessfully() {
        String faqId = "1";
        doNothing().when(faqService).deleteFaq(faqId);

        ResponseEntity<String> response = faqController.deleteFaq(faqId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Faq Deleted Successfully!", response.getBody());
    }

    @Test
    void updateFaq_WithValidIdAndData_UpdatesFaqSuccessfully() {
        String faqId = "1";
        FaqDto faqDto = new FaqDto("Updated Question", "Updated Answer");
        doNothing().when(faqService).updateFaq(faqId, faqDto);

        ResponseEntity<String> response = faqController.updateFaq(faqId, faqDto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Faq Updated Successfully!", response.getBody());
    }

    @Test
    void deleteFaq_WithInvalidId_ReturnsNotFoundError() {
        String faqId = "-1";
        doThrow(new RuntimeException("Faq not found: " + faqId)).when(faqService).deleteFaq(faqId);

        ResponseEntity<String> response = faqController.deleteFaq(faqId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("FAQ not found", response.getBody());
    }

    @Test
    void updateFaq_WithInvalidId_ReturnsNotFoundError() {
        String faqId = "-1";
        FaqDto faqDto = new FaqDto("Updated Question", "Updated Answer");
        doThrow(new RuntimeException("FAQ not found")).when(faqService).updateFaq(faqId, faqDto);

        ResponseEntity<String> response = faqController.updateFaq(faqId, faqDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("FAQ not found", response.getBody());
    }
}


