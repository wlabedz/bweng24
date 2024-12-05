package com.backend.project.service;

import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.exceptions.PhotoNotFoundException;
import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.model.ItemPhoto;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.ItemPhotoRepository;
import com.backend.project.repository.UserRepository;
import com.backend.project.security.JWTGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;


@Service
public class ItemPhotoService {

    private final ItemPhotoRepository itemPhotoRepository;

    @Autowired
    public ItemPhotoService(ItemPhotoRepository itemPhotoRepository){
        this.itemPhotoRepository = itemPhotoRepository;
    }

    public ItemPhoto addPhoto(String content){
        ItemPhoto newPhoto = new ItemPhoto(content);
        return itemPhotoRepository.save(newPhoto);
    }

    public void deletePhotoById(UUID photoId) {
        Optional<ItemPhoto> photo = itemPhotoRepository.findById(photoId);
        if (photo.isPresent()) {
            itemPhotoRepository.delete(photo.get());
        } else {
            throw new PhotoNotFoundException(photoId);
        }
    }

    public ItemPhoto getPhotoById(UUID photoId) {
        return itemPhotoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException(photoId));
    }
}

