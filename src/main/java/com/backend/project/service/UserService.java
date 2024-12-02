package com.backend.project.service;

import com.backend.project.dto.UserDto;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.model.UserEntity;
import com.backend.project.model.UserPhoto;
import com.backend.project.repository.UserRepository;
import com.backend.project.security.JWTGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JWTGenerator jwtGenerator;
    private UserPhotoService userPhotoService;

    public UserService(UserRepository userRepository, UserPhotoService userPhotoService, JWTGenerator jwtGenerator){
        this.userRepository = userRepository;
        this.jwtGenerator = jwtGenerator;
        this.userPhotoService = userPhotoService;
    }

    @Autowired
    public void setUserPhotoService(UserPhotoService userPhotoService) {
        this.userPhotoService = userPhotoService;
    }

    public UserEntity getUserByUsername(String username){
        return userRepository.findAll()
                .stream()
                .filter(userEntity -> userEntity.getUsername().equals(username))
                .findFirst().orElseThrow(() -> new UserNotFoundException(username));
    }

    public UserDto getUserByRequest(HttpServletRequest request) throws UserNotFoundException, InvalidToken {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            throw new InvalidToken("Token body does not comply with assumed format and therefore cannot be validated");
        }

        if (jwtGenerator.validateToken(token)) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            UserEntity user;

            try{
                user = getUserByUsername(username);
            } catch(UserNotFoundException exc){
                throw new UserNotFoundException(exc.getMessage());
            }

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

            UserDto userDTO = new UserDto(user.getUsername(), user.getName(), user.getSurname(), user.getMail(), content);;
            return userDTO;
        } else {
            throw new InvalidToken("Token cannot be validated");
        }
    }



    public UserEntity updateUser(UserEntity user) {
        UserEntity existingUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        existingUser.setName(user.getName());
        existingUser.setSurname(user.getSurname());
        existingUser.setMail(user.getMail());
        existingUser.setPhoto(user.getPhoto());

        return userRepository.save(existingUser);
    }

    public UserDto patchUserPhoto(UserDto userDto){
        UserEntity existingUser = userRepository.findByUsername(userDto.username()).orElseThrow();

        if(existingUser.getPhoto() != null) {
            userPhotoService.deletePhotoById(existingUser.getPhoto());
        }
        existingUser.setPhoto(null);
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
                picture
        );
    }

}
