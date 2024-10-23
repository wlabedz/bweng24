package com.backend.project.service;

import com.backend.project.dto.ReviewDto;
import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.model.Review;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.ReviewRepository;
import com.backend.project.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository){
        this.reviewRepository = reviewRepository;
    }

    public List<Review> getAllReviews(){
        return reviewRepository.findAll();
    }

    public Review addReview(ReviewDto reviewDto, UserEntity user){
        Review review = new Review(user, reviewDto.opinion());

        return reviewRepository.save(review);
    }
}
