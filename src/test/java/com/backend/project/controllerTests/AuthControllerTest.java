package com.backend.project.controllerTests;

import com.backend.project.controller.AuthController;
import com.backend.project.dto.AuthResponseDto;
import com.backend.project.dto.LoginDto;
import com.backend.project.dto.RegisterDto;
import com.backend.project.dto.ChangePasswordDto;
import com.backend.project.exceptions.EmailTakenException;
import com.backend.project.exceptions.InvalidCredentialsException;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.exceptions.UsernameTakenException;
import com.backend.project.service.UserService;
import jakarta.validation.constraints.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_ValidCredentials_ReturnsSuccessResponse() {
        String accessToken = "exampleToken";
        AuthResponseDto authResponseDto = new AuthResponseDto(accessToken);
        LoginDto loginDto = new LoginDto("myUsername", "Password1!");

        when(userService.login(any(LoginDto.class))).thenReturn(authResponseDto);

        AuthResponseDto response = authController.login(loginDto).getBody();

        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals("Bearer: ", response.getTokenType());
    }

    @Test
    void register_ValidData_ReturnsSuccessMessage() {
        RegisterDto registerDto = new RegisterDto(
                "John",
                "Smith",
                "jsmith",
                "test@example.com",
                "Password123!",
                "Mr",
                "US"
        );

        String response = authController.register(registerDto).getBody();

        assertEquals("Successfully registered user", response);
    }

    @Test
    void register_UsernameTaken_ThrowsUsernameTakenException() throws UsernameTakenException, EmailTakenException {
        RegisterDto registerDto = new RegisterDto(
                "John", "Smith", "jsmith", "test@example.com",
                "Password123!", "Mr", "US"
        );

        when(userService.registerUser(any(RegisterDto.class)))
                .thenThrow(new UsernameTakenException("jsmith"));

        ResponseEntity<String> response = authController.register(registerDto);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Username jsmith is already taken", response.getBody());
    }

    @Test
    void register_EmailTaken_ThrowsEmailTakenException() throws UsernameTakenException, EmailTakenException {
        RegisterDto registerDto = new RegisterDto(
                "John", "Smith", "jsmith", "test@example.com",
                "Password123!", "Mr", "US"
        );

        when(userService.registerUser(any(RegisterDto.class)))
                .thenThrow(new EmailTakenException("test@example.com"));

        ResponseEntity<String> emailResponse = authController.register(registerDto);

        assertEquals(HttpStatus.CONFLICT, emailResponse.getStatusCode());
        assertEquals("Email test@example.com is already taken", emailResponse.getBody());
    }

    @Test
    void changePassword_ValidToken_ReturnsSuccessMessage() throws InvalidToken, InvalidCredentialsException {
        String token = "validToken";
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("Password123!", "Password234!", "Password234!");

        when(userService.changePassword(any(ChangePasswordDto.class), eq("Bearer validToken")))
                .thenReturn(null);

        String response = authController.changePassword("Bearer validToken", changePasswordDto).getBody();

        assertEquals("Password successfully changed", response);
    }

    @Test
    void changePassword_InvalidToken_ThrowsInvalidTokenException() throws InvalidToken, InvalidCredentialsException {
        String invalidToken = "invalidToken";
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("Password123!", "Password234!", "Password234!");

        when(userService.changePassword(any(ChangePasswordDto.class), eq("Bearer invalidToken")))
                .thenThrow(new InvalidToken("Invalid or missing token"));

        ResponseEntity<String> response = authController.changePassword("Bearer invalidToken", changePasswordDto);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid or missing token", response.getBody());
    }
}
