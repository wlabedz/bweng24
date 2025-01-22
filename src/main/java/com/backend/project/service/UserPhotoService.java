package com.backend.project.service;

import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.exceptions.FileException;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.model.PhotoUser;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.UserPhotoRepository;
import com.backend.project.repository.UserRepository;
import com.backend.project.security.JWTGenerator;
import com.backend.project.storage.FileStorage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserPhotoService {
    private final UserPhotoRepository photoUserRepository;
    private final JWTGenerator jwtGenerator;
    private final UserRepository userRepository;
    private final FileStorage fileStorage;

    @Autowired
    public UserPhotoService(UserPhotoRepository userPhotoRepository, JWTGenerator jwtGenerator,
                            UserRepository userRepository, FileStorage fileStorage) {
        this.photoUserRepository = userPhotoRepository;
        this.jwtGenerator = jwtGenerator;
        this.userRepository = userRepository;
        this.fileStorage = fileStorage;
    }


    public PhotoUser addPhoto(MultipartFile content, HttpServletRequest request) throws InvalidToken, FailedUploadingPhoto {
        if(!Objects.equals(content.getContentType(), "image/png") && !Objects.equals(content.getContentType(), "image/jpeg") && !Objects.equals(content.getContentType(), "image/gif")){
            throw new FailedUploadingPhoto("Unsupported file format");
        }

        UserEntity user = getUserFromToken(request);
        String externalId;
        try {
            externalId = fileStorage.upload(content);
        }catch(FileException e){
            throw new FailedUploadingPhoto(e.getMessage());
        }

        PhotoUser usph = new PhotoUser(externalId);
        usph.setContentType(content.getContentType());
        usph.setName(content.getOriginalFilename());


        UUID toDelete = user.getPhoto();
        if(toDelete != null){
            photoUserRepository.deleteById(toDelete);
        }
        user.setPhoto(usph.getId());

        userRepository.save(user);

        return photoUserRepository.save(usph);
    }


    public Optional<PhotoUser> getPhotoById(UUID id){
        if(id == null){
            return Optional.empty();
        }
        return photoUserRepository.findById(id);
    }


    public void deletePhoto(HttpServletRequest request) throws InvalidToken, UserNotFoundException {
        UserEntity user = getUserFromToken(request);
        PhotoUser photo = getPhotoById(user.getPhoto()).orElse(null);

        user.setPhoto(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        if(photo != null){
            deletePhotoFromBoth(photo);
        }
    }

    public void deletePhotoAdmin(String username) throws InvalidToken, UserNotFoundException {
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if(user == null){
            throw new UserNotFoundException(username);
        }

        PhotoUser photo = getPhotoById(user.getPhoto()).orElse(null);

        if(photo != null){
            deletePhotoFromBoth(photo);
        }
        user.setPhoto(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void deletePhotoFromBoth(PhotoUser photo){
        fileStorage.deleteFile(photo.getExternalId());
        photoUserRepository.deleteById(photo.getId());
    }


    private UserEntity getUserFromToken(HttpServletRequest request) throws InvalidToken {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            throw new InvalidToken("Token body does not comply with assumed format and therefore cannot be validated");
        }

        String username = jwtGenerator.getUsernameFromJWT(token);
        UserEntity user;

        user = userRepository.findByUsername(username).orElse(null);
        if(user == null){
            throw new UserNotFoundException("User could not have been found.");
        }

        return user;
    }

    public Resource asResource(PhotoUser photo) {
        InputStream stream = fileStorage.download(photo.getExternalId());
        return new InputStreamResource(stream);
    }

    public void deletePhotoById(UUID id){
        PhotoUser photo = getPhotoById(id).orElse(null);
        if(photo != null){
            deletePhotoFromBoth(photo);
        }
    }

}
