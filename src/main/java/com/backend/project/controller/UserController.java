package com.backend.project.controller;

import com.backend.project.dto.UserDto;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.exceptions.NotAllowedException;
import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public ResponseEntity<UserDto> getUser(HttpServletRequest request){
        UserDto user;
        try{
            user = userService.getUserByRequest(request);
        }catch(InvalidToken exception){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }catch (UserNotFoundException exception) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(user);
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
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto updatedUserDto, HttpServletRequest request) {
        try {
            UserDto userEntity = userService.updateUser(updatedUserDto, request);
            return ResponseEntity.ok(userEntity);
        }catch(InvalidToken exception){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }catch(NotAllowedException exception){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

}
