package com.backend.project.service;

import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public UserEntity getUserByUsername(String username){
        return userRepository.findAll()
                .stream()
                .filter(userEntity -> userEntity.getUsername().equals(username))
                .findFirst().orElseThrow(() -> new UserNotFoundException(username));
    }
}
