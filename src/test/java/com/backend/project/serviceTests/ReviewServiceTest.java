package com.backend.project.serviceTests;

import com.backend.project.dto.ReviewDto;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.model.Review;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.ReviewRepository;
import com.backend.project.security.JWTGenerator;
import com.backend.project.service.ReviewService;
import com.backend.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private JWTGenerator jwtGenerator;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private HttpServletRequest request;

    private ReviewDto reviewDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reviewDto = new ReviewDto("This is a review");
    }

    @Test
    void addReview_Success_ReviewAdded() throws InvalidToken, UserNotFoundException {
        String token = "validToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtGenerator.getUsernameFromJWT(token)).thenReturn("username");

        UserEntity user = new UserEntity();
        when(userService.getUserByUsername("myUsername")).thenReturn(user);

        Review review = new Review(UUID.randomUUID(), user, "This is a review", LocalDateTime.now());
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review result = reviewService.addReview(reviewDto, request);

        assertNotNull(result);
        assertEquals(review.getUser(), result.getUser());
        assertEquals(review.getOpinion(), result.getOpinion());
        assertNotNull(result.getCreatedAt());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void addReview_InvalidToken_ThrowsInvalidTokenException() {
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThrows(InvalidToken.class, () -> reviewService.addReview(reviewDto, request));
    }

    @Test
    void addReview_UserNotFound_ThrowsUserNotFoundException() throws InvalidToken {
        String token = "validToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtGenerator.getUsernameFromJWT(token)).thenReturn("myUsername");
        when(userService.getUserByUsername("myUsername")).thenThrow(new UserNotFoundException("nonexistent"));

        assertThrows(UserNotFoundException.class, () -> reviewService.addReview(reviewDto, request));
    }

    @Test
    void getAllReviews_Success_ReviewsRetrieved() {
        Review review1 = new Review(UUID.randomUUID(), new UserEntity(), "Review 1", LocalDateTime.now());
        Review review2 = new Review(UUID.randomUUID(), new UserEntity(), "Review 2", LocalDateTime.now());

        when(reviewRepository.findAll()).thenReturn(List.of(review1, review2));

        List<Review> reviews = reviewService.getAllReviews();

        assertNotNull(reviews);
        assertEquals(2, reviews.size());
        assertTrue(reviews.contains(review1));
        assertTrue(reviews.contains(review2));

        verify(reviewRepository, times(1)).findAll();
    }
}
