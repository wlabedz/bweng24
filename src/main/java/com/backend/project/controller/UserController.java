package com.backend.project.controller;


import com.backend.project.dto.UserDto;
import com.backend.project.model.UserEntity;
import com.backend.project.security.CustomUserDetailsService;
import com.backend.project.security.JWTGenerator;
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

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final JWTGenerator jwtGenerator;

    public UserController(UserService userService, JWTGenerator jwtGenerator) {
        this.userService = userService;
        this.jwtGenerator = jwtGenerator;
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

            if (user != null) {
                UserDto userDTO = new UserDto(user.getUsername(), user.getName(), user.getSurname(), user.getMail(), user.getProfileImagePath());;
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
            try {
                Path dirPath = Paths.get("src/main/resources/uploads");
                File[] filesToDelete = dirPath.toFile().listFiles((dir, name) -> name.startsWith(username));
                if (filesToDelete != null) {
                    for (File file : filesToDelete) {
                        file.delete();
                    }
                }


                String fileName = username + "_" + System.currentTimeMillis() + "_" + photo.getOriginalFilename();
                Path filePath = Paths.get("src/main/resources/uploads", fileName);

                Files.createDirectories(filePath.getParent());
                Files.write(filePath, photo.getBytes());

                String base64Image = null;
                try {
                    byte[] imageBytes = Files.readAllBytes(filePath);
                    base64Image = Base64.getEncoder().encodeToString(imageBytes);

                } catch (Exception e) {
                    base64Image = null;
                }

                user.setProfileImagePath(base64Image);
                userService.updateUser(user);
                return new ResponseEntity<>(base64Image, HttpStatus.OK);

            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>("Error uploading file", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

}
