package com.backend.project.serviceTests;

import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.model.PhotoItem;
import com.backend.project.model.PhotoOffice;
import com.backend.project.repository.ItemPhotoRepository;
import com.backend.project.service.FoundItemService;
import com.backend.project.service.ItemPhotoService;
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

public class ItemPhotoServiceTest {

    private ItemPhotoService itemPhotoService;

    @Mock
    private ItemPhotoRepository itemPhotoRepository;

    @Mock
    private FileStorage fileStorage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        itemPhotoService = new ItemPhotoService(itemPhotoRepository, fileStorage);
    }

    @Test
    void addPhoto_WhenSupportedPhotoFormat_AddsAndReturnsItemPhoto() throws FailedUploadingPhoto {
        MultipartFile multipartFile = mock(MultipartFile.class);
        UUID externalId = UUID.randomUUID();

        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getOriginalFilename()).thenReturn("test.png");
        when(fileStorage.upload(multipartFile)).thenReturn(externalId.toString());

        PhotoItem mockPhotoItem = new PhotoItem(externalId.toString());
        mockPhotoItem.setContentType("image/png");
        mockPhotoItem.setName("test.png");
        when(itemPhotoRepository.save(any(PhotoItem.class))).thenReturn(mockPhotoItem);

        PhotoItem result = itemPhotoService.addPhoto(multipartFile);

        assertNotNull(result);
        assertEquals(externalId.toString(), result.getExternalId());
        assertEquals("image/png", result.getContentType());
        assertEquals("test.png", result.getName());

        verify(fileStorage, times(1)).upload(multipartFile);
        verify(itemPhotoRepository, times(1)).save(any(PhotoItem.class));
    }

    @Test
    void addPhoto_WhenUnsupportedPhotoFormat_ThrowsException() {
        MultipartFile multipartFile = mock(MultipartFile.class);

        when(multipartFile.getContentType()).thenReturn("pdf");

        assertThrows(FailedUploadingPhoto.class, () -> itemPhotoService.addPhoto(multipartFile));

        verify(fileStorage, times(0)).upload(any(MultipartFile.class));
        verify(itemPhotoRepository, times(0)).save(any(PhotoItem.class));
    }


    @Test
    void getPhotoById_WhenPhotoExists_ReturnsPhotoItem(){
        PhotoItem mockPhoto = new PhotoItem("externalId");

        when(itemPhotoRepository.findById(mockPhoto.getId())).thenReturn(Optional.of(mockPhoto));

        PhotoItem result = itemPhotoService.getPhotoById(mockPhoto.getId());

        assertEquals(mockPhoto.getId(), result.getId());
        assertEquals("externalId",result.getExternalId());

        verify(itemPhotoRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void deletePhotoFromBoth_WhenPhotoExists_CallsRepositoryAndStorage() {
        UUID photoId = UUID.randomUUID();
        String externalId = "externalId";
        PhotoItem mockPhoto = new PhotoItem(externalId);
        mockPhoto.setId(photoId);

        itemPhotoService.deletePhotoFromBoth(mockPhoto);

        verify(fileStorage, times(1)).deleteFile(externalId);
        verify(itemPhotoRepository, times(1)).deleteById(photoId);
    }

    @Test
    void deletePhotoById_WhenPhotoExists_CallsRepositoryAndStorage() {
        UUID photoId = UUID.randomUUID();
        PhotoItem mockPhoto = new PhotoItem("externalId");
        mockPhoto.setId(photoId);
        doNothing().when(fileStorage).deleteFile(any(String.class));
        doNothing().when(itemPhotoRepository).deleteById(any(UUID.class));

        when(itemPhotoRepository.findById(photoId)).thenReturn(Optional.of(mockPhoto));

        itemPhotoService.deletePhotoById(photoId);

        verify(fileStorage, times(1)).deleteFile("externalId");
        verify(itemPhotoRepository, times(1)).deleteById(photoId);
    }

    @Test
    void deletePhotoById_WhenPhotoNotFound_DoesNotCallRepositoryAndStorage() {
        UUID photoId = UUID.randomUUID();
        when(itemPhotoRepository.findById(photoId)).thenReturn(Optional.empty());

        itemPhotoService.deletePhotoById(photoId);

        verify(fileStorage, never()).deleteFile(anyString());
        verify(itemPhotoRepository, never()).deleteById(any());
    }

    @Test
    void asResource_ConvertsToResource() {
        PhotoItem mockPhoto = new PhotoItem("externalId");
        InputStream mockStream = new ByteArrayInputStream("content".getBytes());
        when(fileStorage.download("externalId")).thenReturn(mockStream);

        Resource result = itemPhotoService.asResource(mockPhoto);

        assertNotNull(result);
        assertTrue(result instanceof InputStreamResource);
        assertDoesNotThrow(() -> {
            InputStream resourceStream = result.getInputStream();
            assertEquals("content", new String(resourceStream.readAllBytes()));

        verify(fileStorage, times(1)).download("externalId");
        });
    }

}
