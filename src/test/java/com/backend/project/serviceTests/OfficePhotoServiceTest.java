package com.backend.project.serviceTests;

import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.model.PhotoItem;
import com.backend.project.model.PhotoOffice;
import com.backend.project.repository.ItemPhotoRepository;
import com.backend.project.repository.OfficePhotoRepository;
import com.backend.project.service.FoundItemService;
import com.backend.project.service.ItemPhotoService;
import com.backend.project.service.OfficePhotoService;
import com.backend.project.storage.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OfficePhotoServiceTest{

    private OfficePhotoService officePhotoService;

    @Mock
    private OfficePhotoRepository officePhotoRepository;

    @Mock
    private FileStorage fileStorage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        officePhotoService = new OfficePhotoService(officePhotoRepository, fileStorage);
    }

    @Test
    void addPhoto_WhenSupportedPhotoFormat_AddsAndReturnsOfficePhoto() throws FailedUploadingPhoto {
        MultipartFile multipartFile = mock(MultipartFile.class);
        UUID externalId = UUID.randomUUID();

        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getOriginalFilename()).thenReturn("test.png");
        when(fileStorage.upload(multipartFile)).thenReturn(externalId.toString());

        PhotoOffice mockOfficeItem = new PhotoOffice(externalId.toString());
        mockOfficeItem.setContentType("image/png");
        mockOfficeItem.setName("test.png");
        when(officePhotoRepository.save(any(PhotoOffice.class))).thenReturn(mockOfficeItem);

        PhotoOffice result = officePhotoService.addPhoto(multipartFile);

        assertNotNull(result);
        assertEquals(externalId.toString(), result.getExternalId());
        assertEquals("image/png", result.getContentType());
        assertEquals("test.png", result.getName());

        verify(fileStorage, times(1)).upload(multipartFile);
        verify(officePhotoRepository, times(1)).save(any(PhotoOffice.class));
    }

    @Test
    void addPhoto_WhenUnsupportedPhotoFormat_ThrowsException() {
        MultipartFile multipartFile = mock(MultipartFile.class);

        when(multipartFile.getContentType()).thenReturn("pdf");

        assertThrows(FailedUploadingPhoto.class, () -> officePhotoService.addPhoto(multipartFile));

        verify(fileStorage, times(0)).upload(any(MultipartFile.class));
        verify(officePhotoRepository, times(0)).save(any(PhotoOffice.class));
    }


    @Test
    void getPhotoById_WhenPhotoExists_ReturnsPhotoOffice(){
        PhotoOffice mockPhoto = new PhotoOffice("externalId");

        when(officePhotoService.getPhotoById(mockPhoto.getId())).thenReturn(Optional.of(mockPhoto));

        PhotoOffice result = officePhotoService.getPhotoById(mockPhoto.getId()).orElse(null);

        assertNotNull(result);
        assertEquals(mockPhoto.getId(), result.getId());
        assertEquals("externalId",result.getExternalId());

        verify(officePhotoRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void deletePhotoFromBoth_WhenPhotoExists_CallsRepositoryAndStorage() {
        UUID photoId = UUID.randomUUID();
        String externalId = "externalId";
        PhotoOffice mockPhoto = new PhotoOffice(externalId);
        mockPhoto.setId(photoId);

        officePhotoService.deletePhotoFromBoth(mockPhoto);

        verify(fileStorage, times(1)).deleteFile(externalId);
        verify(officePhotoRepository, times(1)).deleteById(photoId);
    }

    @Test
    void deletePhotoById_WhenPhotoExists_CallsRepositoryAndStorage() {
        UUID photoId = UUID.randomUUID();
        PhotoOffice mockPhoto = new PhotoOffice("externalId");
        mockPhoto.setId(photoId);
        doNothing().when(fileStorage).deleteFile(any(String.class));
        doNothing().when(officePhotoRepository).deleteById(any(UUID.class));

        when(officePhotoRepository.findById(photoId)).thenReturn(Optional.of(mockPhoto));

        officePhotoService.deletePhotoById(photoId);

        verify(fileStorage, times(1)).deleteFile("externalId");
        verify(officePhotoRepository, times(1)).deleteById(photoId);
    }

    @Test
    void deletePhotoById_WhenPhotoNotFound_DoesNotCallRepositoryAndStorage() {
        UUID photoId = UUID.randomUUID();
        when(officePhotoRepository.findById(photoId)).thenReturn(Optional.empty());

        officePhotoService.deletePhotoById(photoId);

        verify(fileStorage, never()).deleteFile(anyString());
        verify(officePhotoRepository, never()).deleteById(any());
    }

    @Test
    void asResource_ConvertsToResource() {
        PhotoOffice mockPhoto = new PhotoOffice("externalId");
        InputStream mockStream = new ByteArrayInputStream("content".getBytes());
        when(fileStorage.download("externalId")).thenReturn(mockStream);

        Resource result = officePhotoService.asResource(mockPhoto);

        assertNotNull(result);
        assertTrue(result instanceof InputStreamResource);
        assertDoesNotThrow(() -> {
            InputStream resourceStream = result.getInputStream();
            assertEquals("content", new String(resourceStream.readAllBytes()));

            verify(fileStorage, times(1)).download("externalId");
        });
    }
}