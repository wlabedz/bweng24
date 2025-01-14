package com.backend.project.controllerTests;

import com.backend.project.controller.FoundItemController;
import com.backend.project.dto.FoundItemDto;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.model.FoundItem;
import com.backend.project.model.ItemPhoto;
import com.backend.project.model.Roles;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.UserRepository;
import com.backend.project.security.JWTGenerator;
import com.backend.project.service.FoundItemService;
import com.backend.project.service.ItemPhotoService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FoundItemControllerTest {

    @Mock
    private FoundItemService foundItemService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemPhotoService itemPhotoService;

    @Mock
    private JWTGenerator jwtGenerator;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private FoundItemController foundItemController;

    private FoundItemDto testItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testItem = new FoundItemDto(
                UUID.randomUUID(),
                "Black iPhone",
                "Electronics",
                "Phone found on the bench around 2pm.",
                "Office A",
                "photo_base64",
                LocalDate.now(),
                "Botanic garden"
        );
    }

    @Test
    void getItems_WhenCalled_ReturnsListOfItems() {
        when(foundItemService.getAllItems()).thenReturn(List.of(testItem));

        ResponseEntity<List<FoundItemDto>> response = foundItemController.getItems();

        assertNotNull(response);
        assertEquals(1, response.getBody().size());
        assertEquals(testItem.name(), response.getBody().get(0).name());
        verify(foundItemService, times(1)).getAllItems();
    }

    @Test
    void getItemById_WhenItemExists_ReturnsItem() {
        when(foundItemService.getItemById(testItem.id())).thenReturn(testItem);

        ResponseEntity<FoundItemDto> response = foundItemController.getItemById(testItem.id());

        assertNotNull(response);
        assertEquals(testItem.name(), response.getBody().name());
        verify(foundItemService, times(1)).getItemById(testItem.id());
    }

    @Test
    void addItem_WhenValidRequest_ReturnsCreatedStatus() throws Exception {
        String token = "Bearer validToken";
        FoundItemDto itemDto = new FoundItemDto(UUID.randomUUID(), "Black iPhone", "Electronics", "Phone found on the bench around 2pm.", "Office A", "photo_base64", LocalDate.now(), "Botanic garden");

        UserEntity mockUser = new UserEntity();
        mockUser.setUsername("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));

        UUID photoId = UUID.randomUUID();
        when(itemPhotoService.addPhoto(any())).thenReturn(new ItemPhoto(photoId.toString()));

        FoundItem newItem = new FoundItem(mockUser, "Black iPhone", "Electronics", "Phone found on the bench around 2pm.", "Office A", photoId, LocalDate.now(), "Botanic garden");
        when(foundItemService.addItem(any(), any())).thenReturn(newItem);

        when(request.getHeader("Authorization")).thenReturn(token);

        ResponseEntity<Void> response = foundItemController.addItem(request, itemDto);

        assertNotNull(response);
        assertEquals(201, response.getStatusCodeValue());
        verify(foundItemService, times(1)).addItem(any(), any());
    }

    @Test
    void addItem_WhenValidToken_InvokesJwtValidation() {
        String token = "Bearer someToken";

        when(jwtGenerator.validateToken(token)).thenReturn(true);

        boolean isValid = jwtGenerator.validateToken(token);

        assertTrue(isValid);
        verify(jwtGenerator, times(1)).validateToken(token);
    }

    @Test
    void deleteItemById_WhenItemExists_ReturnsNoContentStatus() throws InvalidToken {
        doNothing().when(foundItemService).deleteItem(testItem.id());

        ResponseEntity<Void> response = foundItemController.deleteItemById(testItem.id());

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(foundItemService, times(1)).deleteItem(testItem.id());
    }
}
