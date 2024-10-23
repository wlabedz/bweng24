package com.backend.project.controller;


import com.backend.project.dto.OfficeDto;
import com.backend.project.dto.ReviewDto;
import com.backend.project.model.Office;
import com.backend.project.model.Review;
import com.backend.project.model.UserEntity;
import com.backend.project.security.CustomUserDetailsService;
import com.backend.project.security.JWTGenerator;
import com.backend.project.service.ReviewService;
import com.backend.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;
    private final JWTGenerator jwtGenerator;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, JWTGenerator jwtGenerator, UserService userService) {
        this.reviewService = reviewService;
        this.jwtGenerator = jwtGenerator;
        this.userService = userService;
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getAllReviews(){
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @PostMapping("/reviews")
    public ResponseEntity<Review> addReview(HttpServletRequest request,@RequestBody @Valid ReviewDto reviewDto) {

        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        UserEntity user;
        if (token != null && jwtGenerator.validateToken(token)) {
            String username = jwtGenerator.getUsernameFromJWT(token);

            user = userService.getUserByUsername(username);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String id = reviewService.addReview(reviewDto, user).getId().toString();
        return ResponseEntity
                .created(URI.create("/reviews/" + id))
                .build();
    }

}
