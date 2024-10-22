package com.backend.project.controller;


import com.backend.project.dto.AuthResponseDto;
import com.backend.project.dto.LoginDto;
import com.backend.project.dto.RegisterDto;
import com.backend.project.model.Roles;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.RoleRepository;
import com.backend.project.repository.UserRepository;
import com.backend.project.security.JWTGenerator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthenticationManager authenticationmanager;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    private JWTGenerator jwtGenerator;


    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                          JWTGenerator jwtGenerator){
        this.authenticationmanager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
    }

    @PostMapping("login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginDto loginDto){
        Authentication authentication = authenticationmanager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.username(),
                        loginDto.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);
        return new ResponseEntity<>(new AuthResponseDto(token), HttpStatus.OK);
    }

    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterDto registerDto){
        if(userRepository.existsByUsername(registerDto.username())){
            return new ResponseEntity<>("Username is taken", HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByMail(registerDto.mail())){
            return new ResponseEntity<>("Email is taken", HttpStatus.BAD_REQUEST);
        }

        UserEntity user = new UserEntity(registerDto.name(), registerDto.surname(), registerDto.mail(), registerDto.username(),passwordEncoder.encode(registerDto.password()));

        Roles roles = roleRepository.findByName("USER").get();
        user.setRoles(Collections.singletonList(roles));

        userRepository.save(user);

        return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
    }
}

