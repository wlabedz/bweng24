package com.backend.project.service;

import com.backend.project.dto.*;
import com.backend.project.exceptions.*;
import com.backend.project.model.Roles;
import com.backend.project.model.UserEntity;
import com.backend.project.model.UserPhoto;
import com.backend.project.repository.RoleRepository;
import com.backend.project.repository.UserRepository;
import com.backend.project.security.JWTGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JWTGenerator jwtGenerator;
    private final UserPhotoService userPhotoService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserService(UserRepository userRepository, UserPhotoService userPhotoService,
                       JWTGenerator jwtGenerator, PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository, AuthenticationManager authenticationManager){
        this.userRepository = userRepository;
        this.jwtGenerator = jwtGenerator;
        this.userPhotoService = userPhotoService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
    }

    public UserEntity getUserByUsername(String username){
        return userRepository.findAll()
                .stream()
                .filter(userEntity -> userEntity.getUsername().equals(username))
                .findFirst().orElseThrow(() -> new UserNotFoundException(username));
    }

    public UserDto getUserByRequest(HttpServletRequest request) throws UserNotFoundException, InvalidToken {
        UserEntity user = getUserFromToken(request);

        UserPhoto usph = null;

        if(user.getPhoto() != null){
            usph = userPhotoService.getPhotoById(user.getPhoto()).orElse(null);
        }

        String content;
        if(usph != null){
            content = usph.getContent();
        }else{
            content = null;
        }

        UserDto userDTO = new UserDto(user.getUsername(), user.getName(), user.getSurname(), user.getMail(), content, user.getSalutation(), user.getCountry());;
        return userDTO;
    }


    public UserDto updateUser(UserDto userDto, HttpServletRequest request) throws InvalidToken, NotAllowedException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            throw new InvalidToken("Token body does not comply with assumed format and therefore cannot be validated");
        }

        String username = jwtGenerator.getUsernameFromJWT(token);

        List<String> roles = jwtGenerator.getRolesFromJWT(token);

        if(!roles.contains("ADMIN") && !Objects.equals(username, userDto.username())){
            throw new NotAllowedException(username);
        }

        UserEntity existingUser = userRepository.findByUsername(userDto.username()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if(existingUser.getPhoto() != null && userDto.photo() == null) {
            userPhotoService.deletePhotoById(existingUser.getPhoto());
            existingUser.setPhoto(null);
        }

        existingUser.setName(userDto.name());
        existingUser.setSurname(userDto.surname());
        existingUser.setMail(userDto.mail());
        existingUser.setCountry(userDto.country());
        existingUser.setSalutation(userDto.salutation());
        existingUser.setUpdatedAt(LocalDateTime.now());

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
            userPhotoService.deletePhotoById(userToDelete.getPhoto());
        }
        userRepository.deleteById(userToDelete.getId());
    }

    public UserEntity registerUser(RegisterDto registerDto) throws UsernameTakenException, EmailTakenException {
        if(userRepository.existsByUsername(registerDto.username())){
            throw new UsernameTakenException(registerDto.username());
        }

        if(userRepository.existsByMail(registerDto.mail())){
            throw new EmailTakenException(registerDto.mail());
        }

        UserEntity user = new UserEntity(registerDto.name(), registerDto.surname(),
                registerDto.mail(), registerDto.username(),passwordEncoder.encode(registerDto.password()),
                registerDto.salutation(), registerDto.country());

        Roles roles;

        if(registerDto.username().startsWith("admin")){
            roles = roleRepository.findByName("ADMIN").orElse(null);
        }
        else{
            roles = roleRepository.findByName("USER").orElse(null);
        }

        user.setRoles(Collections.singletonList(roles));
        return userRepository.save(user);
    }


    public String changePassword(ChangePasswordDto changePasswordDto, String token) throws InvalidToken, UsernameNotFoundException, InvalidCredentialsException {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtGenerator.validateToken(jwt)) {
            throw new InvalidToken("Invalid or expired token");
        }

        String username = jwtGenerator.getUsernameFromJWT(jwt);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(changePasswordDto.oldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Old password is incorrect");
        }

        String newPassword = passwordEncoder.encode(changePasswordDto.newPassword());
        user.setPassword(newPassword);
        userRepository.save(user);
        return newPassword;
    }

    public AuthResponseDto login(LoginDto loginDto){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.username(),
                        loginDto.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);

        return new AuthResponseDto(token);
    }

    private UserDto mapToDto(UserEntity user) {
        UserPhoto userPhoto = null;
        if(user.getPhoto() != null){
            userPhoto = userPhotoService.getPhotoById(user.getPhoto()).orElse(null);
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
                picture,
                user.getSalutation(),
                user.getCountry()
        );
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
}
