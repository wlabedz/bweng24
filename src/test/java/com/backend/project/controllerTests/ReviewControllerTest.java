package com.backend.project.controllerTests;

import com.backend.project.controller.ReviewController;
import com.backend.project.dto.ReviewDto;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.model.Review;
import com.backend.project.model.UserEntity;
import com.backend.project.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @Mock
    private HttpServletRequest request;

    private ReviewDto reviewDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reviewDto = new ReviewDto("This is a review");
    }

    @Test
    void getAllReviews_Success_ReturnsListOfReviews() {
        when(reviewService.getAllReviews()).thenReturn(List.of(new Review()));

        ResponseEntity<List<Review>> response = reviewController.getAllReviews();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void addReview_Success_ReturnsCreatedReview() throws InvalidToken, UserNotFoundException {
        UserEntity user = new UserEntity();
        Review review = new Review(UUID.randomUUID(), user, "This is a review", LocalDateTime.now());
        when(reviewService.addReview(any(ReviewDto.class), any(HttpServletRequest.class))).thenReturn(review);

        ResponseEntity<Review> response = reviewController.addReview(request, reviewDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(reviewService, times(1)).addReview(any(ReviewDto.class), any(HttpServletRequest.class));
    }

    @Test
    void addReview_InvalidToken_ThrowsUnauthorized() throws InvalidToken, UserNotFoundException {
        doThrow(new InvalidToken("invalidToken")).when(reviewService).addReview(any(ReviewDto.class), any(HttpServletRequest.class));

        ResponseEntity<Review> response = reviewController.addReview(request, reviewDto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void addReview_UserNotFound_ThrowsUnauthorized() throws InvalidToken, UserNotFoundException {
        doThrow(new UserNotFoundException("nonexistent")).when(reviewService).addReview(any(ReviewDto.class), any(HttpServletRequest.class));

        ResponseEntity<Review> response = reviewController.addReview(request, reviewDto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
