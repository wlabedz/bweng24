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

    public UserEntity updateUser(UserEntity user) {
        UserEntity existingUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        existingUser.setName(user.getName());
        existingUser.setSurname(user.getSurname());
        existingUser.setMail(user.getMail());
        existingUser.setPhoto(user.getPhoto());

        return userRepository.save(existingUser);
    }

}
