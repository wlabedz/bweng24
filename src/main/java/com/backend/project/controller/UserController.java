package com.backend.project.controller;


import com.backend.project.dto.DistrictDto;
import com.backend.project.dto.OfficeRetDto;
import com.backend.project.dto.UserDto;
import com.backend.project.model.Office;
import com.backend.project.model.OfficePhoto;
import com.backend.project.model.UserEntity;
import com.backend.project.model.UserPhoto;
import com.backend.project.security.CustomUserDetailsService;
import com.backend.project.security.JWTGenerator;
import com.backend.project.service.UserPhotoService;
import com.backend.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final JWTGenerator jwtGenerator;
    private final UserPhotoService userPhotoService;

    public UserController(UserService userService, JWTGenerator jwtGenerator, UserPhotoService userPhotoService) {
        this.userService = userService;
        this.jwtGenerator = jwtGenerator;
        this.userPhotoService = userPhotoService;
    }

    @GetMapping("/user")
    public ResponseEntity<UserDto> getUser(HttpServletRequest request){

        String token = request.getHeader(HttpHeaders.AUTHORIZATION);


        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }


        if (token != null && jwtGenerator.validateToken(token)) {
            String username = jwtGenerator.getUsernameFromJWT(token);

            UserEntity user = userService.getUserByUsername(username);
            UserPhoto usph = null;

            if(user.getPhoto() != null){
                usph = userPhotoService.getPhotoById(user.getPhoto()).orElse(null);
            }
            String content;
            if( usph != null){
                content = usph.getContent();
            }else{
                content = null;
            }

            if (user != null) {
                UserDto userDTO = new UserDto(user.getUsername(), user.getName(), user.getSurname(), user.getMail(), content);;
                return new ResponseEntity<>(userDTO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/user/photo")
    public ResponseEntity<String> addPhoto(@RequestParam("photo") MultipartFile photo, HttpServletRequest request){
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token != null && jwtGenerator.validateToken(token)) {
            String username = jwtGenerator.getUsernameFromJWT(token);

            UserEntity user = userService.getUserByUsername(username);

            if (user != null) {
                if (photo.isEmpty()) {
                    return new ResponseEntity<>("No file uploaded", HttpStatus.BAD_REQUEST);
                }
            }
            String base64Image;
            try {
                byte[] imageBytes = photo.getBytes();
                base64Image = Base64.getEncoder().encodeToString(imageBytes);
            } catch (Exception e) {
                base64Image = null;
            }
            if(base64Image != null){
                UserPhoto usph = userPhotoService.addPhoto(base64Image);
                assert user != null;

                UUID toDelete = user.getPhoto();
                userPhotoService.deletePhotoById(toDelete);
                user.setPhoto(usph.getId());
            }
            else{
                user.setPhoto(null);
            }


            userService.updateUser(user);
            return new ResponseEntity<>(base64Image, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAll();

        return ResponseEntity.ok(users);
    }


    @DeleteMapping("/users")
    public void deleteUserById(@RequestBody String username){
        userService.removeByUsername(username);
    }

    @PutMapping("/users")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto updatedUserDto) {
        UserDto userEntity = userService.patchUserPhoto(updatedUserDto);
        return ResponseEntity.ok(userEntity);
    }

}
