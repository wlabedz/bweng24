package com.backend.project.serviceTests;

import com.backend.project.dto.*;
import com.backend.project.exceptions.*;
import com.backend.project.model.*;
import com.backend.project.repository.RoleRepository;
import com.backend.project.repository.UserRepository;
import com.backend.project.security.JWTGenerator;
import com.backend.project.service.UserPhotoService;
import com.backend.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTGenerator jwtGenerator;

    @Mock
    private UserPhotoService userPhotoService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserByUsername_Success_UserFound() {
        String username = "myUsername";
        UserEntity mockUser = new UserEntity();
        mockUser.setUsername(username);

        when(userRepository.findAll()).thenReturn(List.of(mockUser));

        UserEntity user = userService.getUserByUsername(username);

        assertNotNull(user);
        assertEquals(username, user.getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserByUsername_UserNotFound_ThrowsUserNotFoundException() {
        String username = "nonexistent";

        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByUsername(username));
    }

    @Test
    void getUserByRequest_Success_UserDtoReturned() throws Exception {
        String username = "myUser";
        String photoContent = "photoContent";
        UserEntity mockUser = new UserEntity();
        mockUser.setUsername(username);
        UserPhoto mockPhoto = new UserPhoto(photoContent);

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer validToken");
        when(jwtGenerator.getUsernameFromJWT("validToken")).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(userPhotoService.getPhotoById(mockUser.getPhoto())).thenReturn(Optional.of(mockPhoto));

        UserDto userDto = userService.getUserByRequest(mockRequest);

        assertNotNull(userDto);
        assertEquals(username, userDto.username());
        verify(jwtGenerator, times(1)).getUsernameFromJWT("validToken");
    }

    @Test
    void removeByUsername_Success_UserRemoved() {
        String username = "myUsername";
        UserEntity mockUser = new UserEntity();
        mockUser.setUsername(username);
        mockUser.setId(UUID.randomUUID());

        when(userRepository.findAll()).thenReturn(List.of(mockUser));

        userService.removeByUsername(username);

        verify(userRepository, times(1)).deleteById(mockUser.getId());
    }

    @Test
    void registerUser_Success_UserRegistered() throws UsernameTakenException, EmailTakenException {
        RegisterDto registerDto = new RegisterDto(
                "John",
                "Smith",
                "jsmith",
                "test@example.com",
                "Password123!",
                "Mr",
                "US"
        );
        UserEntity mockUser = new UserEntity(registerDto.getName(), registerDto.getSurname(),
                registerDto.getMail(), registerDto.getUsername(), "encodedPassword",
                registerDto.getSalutation(), registerDto.getCountry());
        Roles role = new Roles("USER");

        when(userRepository.existsByUsername(registerDto.getUsername())).thenReturn(false);
        when(userRepository.existsByMail(registerDto.getMail())).thenReturn(false);
        when(passwordEncoder.encode(registerDto.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(UserEntity.class))).thenReturn(mockUser);

        UserEntity registeredUser = userService.registerUser(registerDto);

        assertNotNull(registeredUser);
        assertEquals(registerDto.getUsername(), registeredUser.getUsername());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void login_Success_TokenGenerated() {
        LoginDto loginDto = new LoginDto("jsmith", "Password123!");
        Authentication mockAuthentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(jwtGenerator.generateToken(mockAuthentication)).thenReturn("jwtToken");

        AuthResponseDto response = userService.login(loginDto);

        assertNotNull(response);
        assertEquals("jwtToken", response.getAccessToken());
    }
}
