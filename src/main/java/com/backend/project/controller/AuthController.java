package com.backend.project.controller;


import com.backend.project.dto.AuthResponseDto;
import com.backend.project.dto.LoginDto;
import com.backend.project.dto.RegisterDto;
import com.backend.project.dto.ChangePasswordDto;
import com.backend.project.exceptions.*;
import com.backend.project.model.Roles;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.RoleRepository;
import com.backend.project.repository.UserRepository;
import com.backend.project.security.JWTGenerator;
import com.backend.project.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginDto loginDto){
        AuthResponseDto authResponseDto = userService.login(loginDto);
        return new ResponseEntity<>(authResponseDto, HttpStatus.OK);
    }

    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterDto registerDto){
        try{
            userService.registerUser(registerDto);
            return ResponseEntity.ok("Successfully registered user");
        }catch(UsernameTakenException | EmailTakenException exception){
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestHeader("Authorization") String token,
                                             @RequestBody ChangePasswordDto changePasswordDto) {
        try{
            userService.changePassword(changePasswordDto,token);
            return ResponseEntity.ok("Password successfully changed");
        }catch(InvalidToken | InvalidCredentialsException | UsernameNotFoundException exception){
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

}

