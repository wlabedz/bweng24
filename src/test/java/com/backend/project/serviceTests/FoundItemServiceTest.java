package com.backend.project.serviceTests;

import com.backend.project.dto.FoundItemDto;
import com.backend.project.dto.UserDto;
import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.exceptions.ItemNotFoundException;
import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.model.FoundItem;
import com.backend.project.model.ItemPhoto;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.FoundItemRepository;
import com.backend.project.repository.UserRepository;
import com.backend.project.security.JWTGenerator;
import com.backend.project.service.FoundItemService;
import com.backend.project.service.ItemPhotoService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

class FoundItemServiceTest {

    private FoundItemService foundItemService;

    @Mock
    private FoundItemRepository foundItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemPhotoService itemPhotoService;

    @Mock
    private JWTGenerator jwtGenerator;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        foundItemService = new FoundItemService(foundItemRepository, userRepository, itemPhotoService, jwtGenerator);
    }

    @Test
    void getItemById_WhenItemExists_ReturnsFoundItemDto() {
        UUID id = UUID.randomUUID();
        UUID photoId = UUID.randomUUID();
        UserEntity user1 = new UserEntity("John", "Smith", "john@example.com","jsmith", "Password1!", "Mr.", "USA");
        FoundItem foundItem = new FoundItem(
                id, "Black iPhone", "Electronics", "Phone found on the bench around 2pm.", "Office A",
                null, LocalDate.now(), "Botanic garden", user1, LocalDate.now().atStartOfDay(), LocalDateTime.now()
        );

        when(foundItemRepository.findById(id)).thenReturn(Optional.of(foundItem));

        FoundItemDto result = foundItemService.getItemById(id);

        assertEquals("Black iPhone", result.name());
        verify(foundItemRepository, times(1)).findById(id);
    }

    @Test
    void getItemById_WhenItemNotFound_ThrowsItemNotFoundException() {
        UUID id = UUID.randomUUID();
        when(foundItemRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> foundItemService.getItemById(id));
    }

    @Test
    void deleteItem_WhenItemExists_CallsRepositoryDelete() throws InvalidToken {
        UUID id = UUID.randomUUID();
        FoundItem foundItem = new FoundItem();
        foundItem.setId(id);

        when(foundItemRepository.findById(id)).thenReturn(Optional.of(foundItem));

        foundItemService.deleteItem(id);

        verify(foundItemRepository, times(1)).deleteById(id);
    }

    @Test
    void addItem_WhenValidTokenAndUser_AddsAndReturnsFoundItem() throws InvalidToken, UserNotFoundException, FailedUploadingPhoto {
        UUID photoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "Bearer someToken";
        UserEntity user = new UserEntity();
        user.setId(userId);
        ItemPhoto photo = new ItemPhoto("photoContent");

        FoundItemDto foundItemDto = new FoundItemDto(null, "Item", "Category", "Description", "Office", "photoContent", LocalDate.now(), "Place");
        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtGenerator.validateToken("someToken")).thenReturn(true);
        when(jwtGenerator.getUsernameFromJWT("someToken")).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(itemPhotoService.addPhoto("photoContent")).thenReturn(photo);

        FoundItem savedItem = new FoundItem();
        when(foundItemRepository.save(any(FoundItem.class))).thenReturn(savedItem);

        FoundItem result = foundItemService.addItem(foundItemDto, request);

        assertNotNull(result);
        verify(foundItemRepository, times(1)).save(any(FoundItem.class));
    }

    @Test
    void updateItemDescription_WhenItemExists_UpdatesDescription() {
        UUID id = UUID.randomUUID();
        FoundItem foundItem = new FoundItem();
        foundItem.setId(id);
        foundItem.setDescription("Old Description");

        when(foundItemRepository.findById(id)).thenReturn(Optional.of(foundItem));
        when(foundItemRepository.save(foundItem)).thenReturn(foundItem);

        FoundItem updatedItem = foundItemService.updateItemDescription(id, "New Description");

        assertNotNull(updatedItem);
        assertEquals("New Description", updatedItem.getDescription());
        verify(foundItemRepository, times(1)).save(foundItem);
    }

    @Test
    void getAllItems_WhenItemsExist_ReturnsAllItems() {
        FoundItem item1 = new FoundItem(UUID.randomUUID(), "Item1", "Category1", "Description1", "Office1", null, LocalDate.now(), "Place1", null, null, null);
        FoundItem item2 = new FoundItem(UUID.randomUUID(), "Item2", "Category2", "Description2", "Office2", null, LocalDate.now(), "Place2", null, null, null);

        when(foundItemRepository.findAll()).thenReturn(List.of(item1, item2));

        List<FoundItemDto> items = foundItemService.getAllItems();

        assertEquals(2, items.size());
        assertEquals("Item1", items.get(0).name());
        assertEquals("Item2", items.get(1).name());
    }

    @Test
    void searchItems_WhenFilterCriteriaMatches_ReturnsFilteredItems() {
        UUID userId = UUID.randomUUID();

        FoundItem item1 = new FoundItem(UUID.randomUUID(), "Item1", "Category1", "Description1", "Office1", UUID.randomUUID(), LocalDate.of(2023, 1, 1), "Place1", null, null, null);
        FoundItem item2 = new FoundItem(UUID.randomUUID(), "Item2", "Category2", "Description2", "Office2", UUID.randomUUID(), LocalDate.of(2023, 2, 1), "Place2", null, null, null);

        when(itemPhotoService.getPhotoById(item1.getPhotoId())).thenReturn(new ItemPhoto("PhotoContent1"));
        when(itemPhotoService.getPhotoById(item2.getPhotoId())).thenReturn(new ItemPhoto("PhotoContent2"));

        when(foundItemRepository.findAll()).thenReturn(List.of(item1, item2));

        List<FoundItemDto> items = foundItemService.searchItems("Category1", null, null, LocalDate.of(2023, 1, 1), null);

        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("Item1", items.get(0).name());
        assertEquals("PhotoContent1", items.get(0).photo());
    }

}
