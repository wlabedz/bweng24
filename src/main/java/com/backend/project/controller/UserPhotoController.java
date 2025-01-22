package com.backend.project.controller;

import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.model.PhotoUser;
import com.backend.project.service.UserPhotoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class UserPhotoController {

    private final UserPhotoService userPhotoService;

    @Autowired
    public UserPhotoController(UserPhotoService userPhotoService){
        this.userPhotoService = userPhotoService;
    }


    @PostMapping("/userphotos")
    public ResponseEntity<PhotoUser> addPhoto(@RequestParam("photo") MultipartFile photo, HttpServletRequest request){
        PhotoUser photoUser;
        try{
            photoUser = userPhotoService.addPhoto(photo,request);
        }catch(InvalidToken exception){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }catch (FailedUploadingPhoto exception) {
            return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }catch (UserNotFoundException exception){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(photoUser, HttpStatus.CREATED);
    }

    @DeleteMapping("/userphotos")
    public ResponseEntity<String> deleteUserPhoto(HttpServletRequest request) {
        try{
            userPhotoService.deletePhoto(request);
        }catch(InvalidToken exception){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }catch (UserNotFoundException exception){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/userphotos/{username}")
    public ResponseEntity<String> deleteUserPhotoAdmin(@PathVariable String username) {
        try{
            userPhotoService.deletePhotoAdmin(username);
        }catch(InvalidToken exception){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }catch (UserNotFoundException exception){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
