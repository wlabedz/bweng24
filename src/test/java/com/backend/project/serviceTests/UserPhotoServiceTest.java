package com.backend.project.serviceTests;

import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.model.Office;
import com.backend.project.model.PhotoOffice;
import com.backend.project.model.PhotoUser;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.OfficePhotoRepository;
import com.backend.project.repository.UserPhotoRepository;
import com.backend.project.repository.UserRepository;
import com.backend.project.security.JWTGenerator;
import com.backend.project.service.OfficePhotoService;
import com.backend.project.service.UserPhotoService;
import com.backend.project.storage.FileStorage;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserPhotoServiceTest {

    private UserPhotoService userPhotoService;

    @Mock
    private UserPhotoRepository userPhotoRepository;

    @Mock
    private FileStorage fileStorage;

    @Mock
    private JWTGenerator jwtGenerator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userPhotoService = new UserPhotoService(userPhotoRepository, jwtGenerator, userRepository, fileStorage);
    }

    @Test
    void addPhoto_WhenSupportedPhotoFormat_AddsAndReturnsUserPhoto() throws InvalidToken, FailedUploadingPhoto {
        UUID oldPhotoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "Bearer someToken";
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setPhoto(oldPhotoId);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(userPhotoRepository.save(any(PhotoUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtGenerator.validateToken("someToken")).thenReturn(true);
        when(jwtGenerator.getUsernameFromJWT("someToken")).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(fileStorage.upload(any(MultipartFile.class))).thenReturn("externalId");

        PhotoUser result = userPhotoService.addPhoto(file, request);

        verify(userPhotoRepository, times(1)).deleteById(oldPhotoId);
        assertEquals("photo.jpg", result.getName());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }


    @Test
    void addPhoto_WhenUnsupportedPhotoFormat_ThrowsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/123");

        assertThrows(FailedUploadingPhoto.class, () -> userPhotoService.addPhoto(file, request));
    }

    @Test
    void getPhotoById_WhenPhotoExists_ReturnsPhotoUser() {
        PhotoUser photo = new PhotoUser("externalId");
        UUID id = UUID.randomUUID();
        photo.setId(id);

        when(userPhotoRepository.findById(id)).thenReturn(Optional.of(photo));

        PhotoUser result = userPhotoService.getPhotoById(id).orElse(null);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }


    @Test
    void deletePhotoFromBoth_WhenPhotoExists_CallsRepositoryAndStorage() {
        UUID photoId = UUID.randomUUID();
        String externalId = "externalId";
        PhotoUser mockPhoto = new PhotoUser(externalId);
        mockPhoto.setId(photoId);

        userPhotoService.deletePhotoFromBoth(mockPhoto);

        verify(fileStorage, times(1)).deleteFile(externalId);
        verify(userPhotoRepository, times(1)).deleteById(photoId);
    }

    @Test
    void deletePhotoById_WhenPhotoExists_CallsRepositoryAndStorage() {
        UUID photoId = UUID.randomUUID();
        PhotoUser mockPhoto = new PhotoUser("externalId");
        mockPhoto.setId(photoId);
        doNothing().when(fileStorage).deleteFile(any(String.class));
        doNothing().when(userPhotoRepository).deleteById(any(UUID.class));

        when(userPhotoRepository.findById(photoId)).thenReturn(Optional.of(mockPhoto));

        userPhotoService.deletePhotoById(photoId);

        verify(fileStorage, times(1)).deleteFile("externalId");
        verify(userPhotoRepository, times(1)).deleteById(photoId);
    }

    @Test
    void deletePhotoById_WhenPhotoNotFound_DoesNotCallRepositoryAndStorage() {
        UUID photoId = UUID.randomUUID();
        when(userPhotoRepository.findById(photoId)).thenReturn(Optional.empty());

        userPhotoService.deletePhotoById(photoId);

        verify(fileStorage, never()).deleteFile(anyString());
        verify(userPhotoRepository, never()).deleteById(any());
    }

    @Test
    void asResource_ConvertsToResource() {
        PhotoUser mockPhoto = new PhotoUser("externalId");
        InputStream mockStream = new ByteArrayInputStream("content".getBytes());
        when(fileStorage.download("externalId")).thenReturn(mockStream);

        Resource result = userPhotoService.asResource(mockPhoto);

        assertNotNull(result);
        assertTrue(result instanceof InputStreamResource);
        assertDoesNotThrow(() -> {
            InputStream resourceStream = result.getInputStream();
            assertEquals("content", new String(resourceStream.readAllBytes()));

            verify(fileStorage, times(1)).download("externalId");
        });
    }

    @Test
    void deletePhoto_WhenPhotoExists_DeletesPhoto() throws InvalidToken {
        UUID oldPhotoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "Bearer someToken";
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setPhoto(oldPhotoId);

        PhotoUser photo = new PhotoUser("externalId");
        photo.setId(oldPhotoId);

        when(userPhotoService.getPhotoById(oldPhotoId)).thenReturn(Optional.of(photo));
        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtGenerator.validateToken("someToken")).thenReturn(true);
        when(jwtGenerator.getUsernameFromJWT("someToken")).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        doNothing().when(fileStorage).deleteFile(any(String.class));
        doNothing().when(userPhotoRepository).deleteById(any(UUID.class));

        userPhotoService.deletePhoto(request);

        assertNull(user.getPhoto());
        verify(userRepository, times(1)).save(user);
        verify(userPhotoRepository, times(1)).findById(oldPhotoId);
        verify(fileStorage, times(1)).deleteFile("externalId");
        verify(userPhotoRepository, times(1)).deleteById(oldPhotoId);
    }

    @Test
    void deletePhotoAdmin_WhenPhotoExists_DeletesPhoto() throws InvalidToken {
        UUID oldPhotoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setPhoto(oldPhotoId);

        PhotoUser photo = new PhotoUser("externalId");
        photo.setId(oldPhotoId);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(userPhotoService.getPhotoById(oldPhotoId)).thenReturn(Optional.of(photo));

        doNothing().when(fileStorage).deleteFile(any(String.class));
        doNothing().when(userPhotoRepository).deleteById(any(UUID.class));

        userPhotoService.deletePhotoAdmin("user");

        assertNull(user.getPhoto());
        verify(userRepository, times(1)).save(user);
        verify(userPhotoRepository, times(1)).findById(oldPhotoId);
        verify(fileStorage, times(1)).deleteFile("externalId");
        verify(userPhotoRepository, times(1)).deleteById(oldPhotoId);
    }

}
