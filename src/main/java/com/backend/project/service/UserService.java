package com.backend.project.service;

import com.backend.project.dto.UserDto;
import com.backend.project.exceptions.OfficeNotFoundException;
import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.model.Office;
import com.backend.project.model.Roles;
import com.backend.project.model.UserEntity;
import com.backend.project.model.UserPhoto;
import com.backend.project.repository.RoleRepository;
import com.backend.project.repository.UserPhotoRepository;
import com.backend.project.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserPhotoRepository userPhotoRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserPhotoRepository userPhotoRepository){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userPhotoRepository = userPhotoRepository;
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

    public UserDto patchUserPhoto(UserDto userDto){
        UserEntity existingUser = userRepository.findByUsername(userDto.username()).orElseThrow();

        if(existingUser.getPhoto() != null) {
            userPhotoRepository.deleteById(existingUser.getPhoto());
        }
        existingUser.setPhoto(null);
        UserEntity userEntity = userRepository.save(existingUser);
        return mapToDto(userEntity);
    }

    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> "USER".equals(role.getName()))).map(this::mapToDto)
                .toList();
    }

    public void removeByUsername(String username){
        UserEntity userToDelete =
                userRepository.findAll()
                        .stream()
                        .filter(user -> user.getUsername().equals(username))
                        .findFirst().
                        orElseThrow(() -> new UserNotFoundException(username));
        if(userToDelete.getPhoto() != null){
            userPhotoRepository.deleteById(userToDelete.getPhoto());
        }
        userRepository.deleteById(userToDelete.getId());
    }


    private UserDto mapToDto(UserEntity user) {
        UserPhoto userPhoto = null;
        if(user.getPhoto() != null){
            userPhoto = userPhotoRepository.findById(user.getPhoto()).orElse(null);
        }

        String picture = null;

        if(userPhoto != null){
            picture = userPhoto.getContent();
        }

        return new UserDto(
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getMail(),
                picture
        );
    }

    public void deleteUserPhoto(UserEntity user) {
        UUID photoId = user.getPhoto();

        if (photoId != null) {
            userPhotoRepository.deleteById(photoId);
            user.setPhoto(null);
            userRepository.save(user);
        }
    }

}
