package com.backend.project.controller;

import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.model.UserPhoto;
import com.backend.project.service.UserPhotoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class UserPhotoController {

    private final UserPhotoService userPhotoService;

    public UserPhotoController(UserPhotoService userPhotoService){
        this.userPhotoService = userPhotoService;
    }


    @PostMapping("/userphotos")
    public ResponseEntity<UserPhoto> addPhoto(@RequestParam("photo") MultipartFile photo, HttpServletRequest request){
        UserPhoto userPhoto;
        try{
            userPhoto = userPhotoService.addPhoto(photo,request);
        }catch(InvalidToken exception){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }catch (FailedUploadingPhoto exception) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (UserNotFoundException exception){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(userPhoto, HttpStatus.CREATED);
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

}
