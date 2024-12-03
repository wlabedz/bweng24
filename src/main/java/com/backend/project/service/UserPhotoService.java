package com.backend.project.service;

import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.model.UserEntity;
import com.backend.project.model.UserPhoto;
import com.backend.project.repository.UserPhotoRepository;
import com.backend.project.repository.UserRepository;
import com.backend.project.security.JWTGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserPhotoService {
    private final UserPhotoRepository userPhotoRepository;
    private final JWTGenerator jwtGenerator;
    private final UserRepository userRepository;

    @Autowired
    public UserPhotoService(UserPhotoRepository userPhotoRepository, JWTGenerator jwtGenerator, UserRepository userRepository) {
        this.userPhotoRepository = userPhotoRepository;
        this.jwtGenerator = jwtGenerator;
        this.userRepository = userRepository;
    }

    public List<UserPhoto> getAllPhotos(){
        return userPhotoRepository.findAll();
    }


    public UserPhoto addPhoto(MultipartFile content, HttpServletRequest request) throws InvalidToken, FailedUploadingPhoto {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            throw new InvalidToken("Token body does not comply with assumed format and therefore cannot be validated");
        }

        if (jwtGenerator.validateToken(token)) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            UserEntity user;

            user = userRepository.findByUsername(username).orElse(null);

            if(user == null){
                throw new UserNotFoundException("User could not have been found");
            }

            String base64Image;
            try {
                byte[] imageBytes = content.getBytes();
                base64Image = Base64.getEncoder().encodeToString(imageBytes);
            } catch (Exception e) {
                throw new FailedUploadingPhoto("Photo cannot be converted");
            }

            UserPhoto usph = new UserPhoto(base64Image);
            userPhotoRepository.save(usph);

            UUID toDelete = user.getPhoto();
            if(toDelete != null){
                deletePhotoById(toDelete);
            }
            user.setPhoto(usph.getId());

            userRepository.save(user);

            return userPhotoRepository.save(usph);
        } else {
            throw new InvalidToken("Token cannot be validated");
        }
    }


    public Optional<UserPhoto> getPhotoById(UUID id){
        return userPhotoRepository.findById(id);
    }


    public void deletePhoto(HttpServletRequest request) throws InvalidToken, UserNotFoundException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            throw new InvalidToken("Token body does not comply with assumed format and therefore cannot be validated");
        }

        if (jwtGenerator.validateToken(token)) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            UserEntity user;

            user = userRepository.findByUsername(username).orElse(null);
            if(user == null){
                throw new UserNotFoundException("User could not have been found.");
            }
            deletePhotoById(user.getPhoto());
        }else{
            throw new InvalidToken("Token cannot be validated");
        }
    }

    public void deletePhotoById(UUID id){
        userPhotoRepository.deleteById(id);
    }
}
