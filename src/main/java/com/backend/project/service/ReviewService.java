package com.backend.project.service;

import com.backend.project.dto.ReviewDto;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.model.Review;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.ReviewRepository;
import com.backend.project.security.JWTGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final JWTGenerator jwtGenerator;
    private final UserService userService;

    public ReviewService(ReviewRepository reviewRepository, JWTGenerator jwtGenerator, UserService userService){
        this.reviewRepository = reviewRepository;
        this.jwtGenerator = jwtGenerator;
        this.userService = userService;
    }

    public List<Review> getAllReviews(){
        return reviewRepository.findAll();
    }

    public Review addReview(ReviewDto reviewDto, HttpServletRequest request) throws InvalidToken, UserNotFoundException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            throw new InvalidToken("Token body does not comply with assumed format and therefore cannot be validated");
        }

        if (jwtGenerator.validateToken(token)) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            UserEntity user;

            try{
                user = userService.getUserByUsername(username);
            } catch(UserNotFoundException exc){
                throw new UserNotFoundException(exc.getMessage());
            }

            Review review = new Review(user, reviewDto.opinion());
            return reviewRepository.save(review);
        } else {
            throw new InvalidToken("Token cannot be validated");
        }
    }
}
