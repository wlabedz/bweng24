package com.backend.project.controller;


import com.backend.project.model.UserEntity;
import com.backend.project.security.CustomUserDetailsService;
import com.backend.project.security.JWTGenerator;
import com.backend.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final JWTGenerator jwtGenerator;

    public UserController(UserService userService, JWTGenerator jwtGenerator) {
        this.userService = userService;
        this.jwtGenerator = jwtGenerator;
    }

    @GetMapping("/user")
    public ResponseEntity<UserEntity> getUser(HttpServletRequest request){

        String token = request.getHeader(HttpHeaders.AUTHORIZATION);


        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }


        if (token != null && jwtGenerator.validateToken(token)) {
            String username = jwtGenerator.getUsernameFromJWT(token);

            UserEntity user = userService.getUserByUsername(username);

            if (user != null) {
                return new ResponseEntity<>(user, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }


}
