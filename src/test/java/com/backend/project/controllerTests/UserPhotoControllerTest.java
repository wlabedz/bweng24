package com.backend.project.controllerTests;
import com.backend.project.controller.UserController;
import com.backend.project.controller.UserPhotoController;
import com.backend.project.dto.UserDto;
import com.backend.project.dto.UserPatchDto;
import com.backend.project.dto.changeEmailDto;
import com.backend.project.exceptions.*;
import com.backend.project.model.PhotoUser;
import com.backend.project.service.UserPhotoService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserPhotoControllerTest {

    private UserPhotoController userPhotoController;

    @Mock
    private UserPhotoService userPhotoService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    public void setUp() {
        userPhotoController = new UserPhotoController(userPhotoService);
    }

    @Test
    void addPhoto_Success_ReturnsCreatedPhoto() throws Exception {
        MultipartFile mockPhoto = mock(MultipartFile.class);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        PhotoUser mockPhotoUser = new PhotoUser("externalId");
        mockPhotoUser.setId(UUID.randomUUID());

        when(userPhotoService.addPhoto(mockPhoto, mockRequest)).thenReturn(mockPhotoUser);
        ResponseEntity<PhotoUser> response = userPhotoController.addPhoto(mockPhoto, mockRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userPhotoService).addPhoto(mockPhoto, mockRequest);
    }

    @Test
    void addPhoto_Failed_ReturnsUnauthorizedWhenInvalidToken() throws Exception {
        MultipartFile mockPhoto = mock(MultipartFile.class);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        when(userPhotoService.addPhoto(mockPhoto, mockRequest)).thenThrow(InvalidToken.class);

        ResponseEntity<PhotoUser> response = userPhotoController.addPhoto(mockPhoto, mockRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userPhotoService).addPhoto(mockPhoto, mockRequest);
    }

    @Test
    void addPhoto_Failed_ReturnsUnsupportedMediaTypeWhenFailedUploading() throws Exception {
        MultipartFile mockPhoto = mock(MultipartFile.class);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        when(userPhotoService.addPhoto(mockPhoto, mockRequest)).thenThrow(FailedUploadingPhoto.class);

        ResponseEntity<PhotoUser> response = userPhotoController.addPhoto(mockPhoto, mockRequest);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        verify(userPhotoService).addPhoto(mockPhoto, mockRequest);
    }

    @Test
    void addPhoto_Failed_ReturnsBadRequestWhenUserNotFound() throws Exception {
        MultipartFile mockPhoto = mock(MultipartFile.class);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        when(userPhotoService.addPhoto(mockPhoto, mockRequest)).thenThrow(UserNotFoundException.class);

        ResponseEntity<PhotoUser> response = userPhotoController.addPhoto(mockPhoto, mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userPhotoService).addPhoto(mockPhoto, mockRequest);
    }

    @Test
    void deleteUserPhoto_Success_ReturnsOk() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        doNothing().when(userPhotoService).deletePhoto(mockRequest);

        ResponseEntity<String> response = userPhotoController.deleteUserPhoto(mockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userPhotoService).deletePhoto(mockRequest);
    }

    @Test
    void deleteUserPhoto_Failed_ReturnsUnauthorizedWhenInvalidToken() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        doThrow(InvalidToken.class).when(userPhotoService).deletePhoto(mockRequest);

        ResponseEntity<String> response = userPhotoController.deleteUserPhoto(mockRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userPhotoService).deletePhoto(mockRequest);
    }

    @Test
    void deleteUserPhoto_Failed_ReturnsBadRequestWhenUserNotFound() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        doThrow(UserNotFoundException.class).when(userPhotoService).deletePhoto(mockRequest);

        ResponseEntity<String> response = userPhotoController.deleteUserPhoto(mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userPhotoService).deletePhoto(mockRequest);
    }

    @Test
    void deleteUserPhotoAdmin_Success_ReturnsOkWhenSuccessful() throws Exception {
        String username = "user";

        doNothing().when(userPhotoService).deletePhotoAdmin(username);

        ResponseEntity<String> response = userPhotoController.deleteUserPhotoAdmin(username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userPhotoService).deletePhotoAdmin(username);
    }

    @Test
    void deleteUserPhotoAdmin_Failed_ReturnsUnauthorizedWhenInvalidToken() throws Exception {
        String username = "user";
        doThrow(InvalidToken.class).when(userPhotoService).deletePhotoAdmin(username);
        ResponseEntity<String> response = userPhotoController.deleteUserPhotoAdmin(username);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userPhotoService).deletePhotoAdmin(username);
    }

    @Test
    void deleteUserPhotoAdmin_Failed_ReturnsBadRequestWhenUserNotFound() throws Exception {
        String username = "user";

        doThrow(UserNotFoundException.class).when(userPhotoService).deletePhotoAdmin(username);

        ResponseEntity<String> response = userPhotoController.deleteUserPhotoAdmin(username);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userPhotoService).deletePhotoAdmin(username);
    }

}
