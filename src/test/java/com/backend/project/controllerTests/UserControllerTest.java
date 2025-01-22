package com.backend.project.controllerTests;

import com.backend.project.controller.UserController;
import com.backend.project.dto.UserDto;
import com.backend.project.dto.UserPatchDto;
import com.backend.project.dto.changeEmailDto;
import com.backend.project.exceptions.*;
import com.backend.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Mock
    private HttpServletRequest request;

    private UserDto userDto;
    private UserPatchDto userPatchDto;
    private changeEmailDto changeEmailDto;

    @BeforeEach
    public void setUp() {
        userDto = new UserDto("jsmith", "John", "Smith", "john@example.com", null, "Mr.", "US");
        userPatchDto = new UserPatchDto("jsmith", "John", "Smith", "john@example.com", "Mr.", "US");
        changeEmailDto = new changeEmailDto("john_new@example.com", "Password1!");
    }

    @Test
    public void getUser_Success_ReturnsUserDto() throws UserNotFoundException, InvalidToken {
        when(userService.getUserByRequest(request)).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.getUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto, response.getBody());
    }

    @Test
    public void getUser_InvalidToken_ReturnsUnauthorized() throws InvalidToken, UserNotFoundException {
        when(userService.getUserByRequest(request)).thenThrow(new InvalidToken("invalidToken"));

        ResponseEntity<UserDto> response = userController.getUser(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void getUser_UserNotFound_ReturnsNotFound() throws InvalidToken {
        when(userService.getUserByRequest(request)).thenThrow(new UserNotFoundException("nonexistent"));

        ResponseEntity<UserDto> response = userController.getUser(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void updateUser_Success_ReturnsUpdatedUserDto() throws InvalidToken, NotAllowedException, UsernameForbiddenException, EmailTakenException, UsernameTakenException {
        when(userService.updateUser(eq("jsmith"), any(UserPatchDto.class), eq(request))).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.updateUser("jsmith", userPatchDto, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto, response.getBody());
    }

    @Test
    public void updateUser_InvalidToken_ReturnsUnauthorized() throws InvalidToken, NotAllowedException, UsernameForbiddenException, EmailTakenException, UsernameTakenException {
        when(userService.updateUser(eq("jsmith"), any(UserPatchDto.class), eq(request)))
                .thenThrow(new InvalidToken("invalidToken"));

        ResponseEntity<UserDto> response = userController.updateUser("jsmith", userPatchDto, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void updateUser_NotAllowed_ReturnsForbidden() throws NotAllowedException, InvalidToken, UsernameForbiddenException, EmailTakenException, UsernameTakenException {
        when(userService.updateUser(eq("jsmith"), any(UserPatchDto.class), eq(request)))
                .thenThrow(new NotAllowedException("Not allowed"));

        ResponseEntity<UserDto> response = userController.updateUser("jsmith", userPatchDto, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void changeEmail_Success_ReturnsSuccessMessage() throws InvalidToken, UsernameNotFoundException, InvalidCredentialsException, EmailTakenException {
        when(userService.changeEmail(any(changeEmailDto.class), eq("Bearer token"))).thenReturn("Email updated");

        ResponseEntity<String> response = userController.changeEmail("Bearer token", changeEmailDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email updated", response.getBody());
    }

    @Test
    public void changeEmail_InvalidToken_ReturnsUnauthorized() throws InvalidToken, UsernameNotFoundException, InvalidCredentialsException, EmailTakenException {
        when(userService.changeEmail(any(changeEmailDto.class), eq("Bearer token")))
                .thenThrow(new InvalidToken("Invalid token"));

        ResponseEntity<String> response = userController.changeEmail("Bearer token", changeEmailDto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void changeEmail_UserNotFound_ReturnsUnauthorized() throws InvalidToken, InvalidCredentialsException, EmailTakenException {
        when(userService.changeEmail(any(changeEmailDto.class), eq("Bearer token")))
                .thenThrow(new UsernameNotFoundException("User not found"));

        ResponseEntity<String> response = userController.changeEmail("Bearer token", changeEmailDto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void changeEmail_EmailTaken_ReturnsBadRequest() throws EmailTakenException, InvalidToken, UsernameNotFoundException, InvalidCredentialsException {
        when(userService.changeEmail(any(changeEmailDto.class), eq("Bearer token")))
                .thenThrow(new EmailTakenException("Email already taken"));

        ResponseEntity<String> response = userController.changeEmail("Bearer token", changeEmailDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getAllUsers_Success_ReturnsOk() {
        List<UserDto> mockUsers = List.of(userDto);
        when(userService.getAll()).thenReturn(mockUsers);

        ResponseEntity<List<UserDto>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockUsers.size(), response.getBody().size());
        assertEquals(mockUsers.get(0).username(), response.getBody().get(0).username());
    }

    @Test
    void getAllUsers_Success_ReturnsEmptyWhenNoUsers() {
        List<UserDto> mockUsers = List.of();
        when(userService.getAll()).thenReturn(mockUsers);

        ResponseEntity<List<UserDto>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void deleteUserById_Success_DeletesUser() {
        String usernameToDelete = "user";
        doNothing().when(userService).removeByUsername(usernameToDelete);

        userController.deleteUserById(usernameToDelete);

        verify(userService, times(1)).removeByUsername(usernameToDelete);
    }


}
