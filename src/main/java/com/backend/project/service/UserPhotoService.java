package com.backend.project.service;


import com.backend.project.dto.ReviewDto;
import com.backend.project.model.Review;
import com.backend.project.model.UserEntity;
import com.backend.project.model.UserPhoto;
import com.backend.project.repository.UserPhotoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserPhotoService {
    private UserPhotoRepository userPhotoRepository;

    public UserPhotoService(UserPhotoRepository userPhotoRepository){
        this.userPhotoRepository = userPhotoRepository;
    }

    public List<UserPhoto> getAllPhotos(){
        return userPhotoRepository.findAll();
    }

    public UserPhoto addPhoto(String content){
        UserPhoto usph = new UserPhoto(content);

        return userPhotoRepository.save(usph);
    }

    public Optional<UserPhoto> getPhotoById(UUID id){
        return userPhotoRepository.findById(id);
    }

    public void deletePhotoById(UUID id){
        if(id != null){
            userPhotoRepository.deleteById(id);
        }
    }
}
