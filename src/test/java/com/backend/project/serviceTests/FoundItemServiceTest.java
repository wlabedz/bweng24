package com.backend.project.serviceTests;

import com.backend.project.dto.FoundItemDto;
import com.backend.project.dto.FoundItemRetDto;
import com.backend.project.dto.UserDto;
import com.backend.project.exceptions.*;
import com.backend.project.model.FoundItem;
import com.backend.project.model.PhotoItem;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.FoundItemRepository;
import com.backend.project.repository.UserRepository;
import com.backend.project.security.JWTGenerator;
import com.backend.project.service.FoundItemService;
import com.backend.project.service.ItemPhotoService;
import com.backend.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

        List<FoundItem> toReturn = new ArrayList<>();
        toReturn.add(foundItem);
        when(foundItemRepository.findAll()).thenReturn(toReturn);

        FoundItemRetDto result = foundItemService.getItemById(id);

        assertEquals("Black iPhone", result.name());
        verify(foundItemRepository, times(1)).findAll();
    }

    @Test
    void getItemById_WhenItemNotFound_ReturnsNull() {
        UUID id = UUID.randomUUID();
        when(foundItemRepository.findById(id)).thenReturn(Optional.empty());
        assertNull(foundItemService.getItemById(id));
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
    void deleteItem_WhenItemExistsAndHasPhoto_CallsRepositoryDelete() throws InvalidToken {
        UUID id = UUID.randomUUID();
        FoundItem foundItem = new FoundItem();
        foundItem.setId(id);
        PhotoItem photoItem = new PhotoItem("externalId");
        foundItem.setPhotoId(photoItem.getId());

        when(foundItemRepository.findById(id)).thenReturn(Optional.of(foundItem));
        doNothing().when(itemPhotoService).deletePhotoById(foundItem.getPhotoId());

        foundItemService.deleteItem(id);

        verify(itemPhotoService,times(1)).deletePhotoById(foundItem.getPhotoId());
        verify(foundItemRepository, times(1)).deleteById(id);
    }

    @Test
    void addItem_WhenValidTokenAndUser_AddsAndReturnsFoundItem() throws InvalidToken, UserNotFoundException, FailedUploadingPhoto {
        UUID photoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = "Bearer someToken";
        UserEntity user = new UserEntity();
        user.setId(userId);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");


        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtGenerator.validateToken("someToken")).thenReturn(true);
        when(jwtGenerator.getUsernameFromJWT("someToken")).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        String sampleExternalId = UUID.randomUUID().toString();
        PhotoItem photo = new PhotoItem(sampleExternalId);

        FoundItemDto foundItemDto = new FoundItemDto(
                null, "Item", "Category", "Description", "Office", null, LocalDate.now(), "Place"
        );

        when(itemPhotoService.addPhoto(file)).thenReturn(photo);

        FoundItem savedItem = new FoundItem();
        when(foundItemRepository.save(any(FoundItem.class))).thenReturn(savedItem);

        FoundItem result = foundItemService.addItem(foundItemDto, request, file);

        assertNotNull(result);
        verify(itemPhotoService, times(1)).addPhoto(file);
        verify(foundItemRepository, times(1)).save(any(FoundItem.class));
    }

    @Test
    void updateItem_WhenItemExistsAndUserIsAdmin_UpdatesItem() throws NotAllowedException, InvalidToken {
        UUID userId = UUID.randomUUID();
        String token = "Bearer someToken";
        UserEntity user = new UserEntity();
        user.setId(userId);

        UUID id = UUID.randomUUID();
        FoundItem foundItem = new FoundItem();
        foundItem.setId(id);
        foundItem.setDescription("Old Description");
        foundItem.setCategory("Old Category");
        foundItem.setFoundPlace("Old Place");
        foundItem.setFoundDate(LocalDate.of(2000,10,10));
        foundItem.setUser(user);

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtGenerator.validateToken("someToken")).thenReturn(true);
        when(jwtGenerator.getUsernameFromJWT("someToken")).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        List<String> roles = new ArrayList<>();
        roles.add("ADMIN");

        when(jwtGenerator.getRolesFromJWT(token)).thenReturn(roles);

        when(foundItemRepository.findById(id)).thenReturn(Optional.of(foundItem));
        when(foundItemRepository.save(foundItem)).thenReturn(foundItem);

        FoundItemRetDto updatedItem = foundItemService.updateItem(id, new FoundItemDto(id, foundItem.getName(),"New Category", "New Description", "office", null,LocalDate.of(2020,10,10),"New Place"),request);

        assertNotNull(updatedItem);
        assertEquals("New Description", updatedItem.description());
        assertEquals("New Category", updatedItem.category());
        assertEquals("New Place", updatedItem.foundPlace());
        assertEquals(LocalDate.of(2020,10,10), updatedItem.foundDate());
        verify(foundItemRepository, times(1)).save(foundItem);
    }

    @Test
    void getAllItems_WhenItemsExist_ReturnsAllItems() {
        FoundItem item1 = new FoundItem(UUID.randomUUID(), "Item1", "Category1", "Description1", "Office1", null, LocalDate.now(), "Place1", null, null, null);
        FoundItem item2 = new FoundItem(UUID.randomUUID(), "Item2", "Category2", "Description2", "Office2", null, LocalDate.now(), "Place2", null, null, null);

        when(foundItemRepository.findAll()).thenReturn(List.of(item1, item2));

        List<FoundItemRetDto> items = foundItemService.getAllItems().orElse(new ArrayList<>());

        assertEquals(2, items.size());
        assertEquals("Item1", items.get(0).name());
        assertEquals("Item2", items.get(1).name());
    }

    @Test
    void searchItems_WhenFilterCriteriaMatches_ReturnsFilteredItems() throws IOException {
        UUID userId = UUID.randomUUID();

        FoundItem item1 = new FoundItem(UUID.randomUUID(), "Item1", "Category1", "Description1", "Office1", UUID.randomUUID(), LocalDate.of(2023, 1, 1), "Place1", null, null, null);
        FoundItem item2 = new FoundItem(UUID.randomUUID(), "Item2", "Category2", "Description2", "Office2", UUID.randomUUID(), LocalDate.of(2023, 2, 1), "Place2", null, null, null);

        PhotoItem photo1 = new PhotoItem("Photo1");
        photo1.setName("photo1");
        PhotoItem photo2 = new PhotoItem("Photo2");
        photo2.setName("photo2");

        when(itemPhotoService.getPhotoById(item1.getPhotoId())).thenReturn(photo1);
        when(itemPhotoService.getPhotoById(item2.getPhotoId())).thenReturn(photo2);

        when(foundItemRepository.findAll()).thenReturn(List.of(item1, item2));
        Resource r1 = new ByteArrayResource(photo1.getName().getBytes());
        when(itemPhotoService.asResource(photo1)).thenReturn(r1);

        Resource r2 = new ByteArrayResource(photo2.getName().getBytes());
        when(itemPhotoService.asResource(photo2)).thenReturn(r2);

        List<FoundItemRetDto> items = foundItemService.searchItems("Category1", null, null, LocalDate.of(2023, 1, 1), null).orElse(new ArrayList<>());

        String r1encoded = Base64.getEncoder().encodeToString(r1.getContentAsByteArray());
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("Item1", items.get(0).name());
        assertEquals(r1encoded, items.get(0).photo());
    }

    @Test
    void getItemsByUser_shouldReturnItemsForValidUser() throws Exception {
        String validToken = "Bearer token";

        when(request.getHeader("Authorization")).thenReturn(validToken);
        when(jwtGenerator.validateToken("token")).thenReturn(true);
        when(jwtGenerator.getUsernameFromJWT("token")).thenReturn("user");

        UserEntity mockUser = new UserEntity();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(mockUser));

        FoundItem mockItem = new FoundItem();
        mockItem.setId(UUID.randomUUID());
        mockItem.setUser(mockUser);
        mockItem.setName("Book");
        mockItem.setCategory("Books");

        when(foundItemRepository.findAll()).thenReturn(List.of(mockItem));

        Optional<List<FoundItemRetDto>> result = foundItemService.getItemsByUser(request);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("Book", result.get().get(0).name());
        assertEquals("Books", result.get().get(0).category());

        verify(jwtGenerator).validateToken("token");
        verify(userRepository).findByUsername("user");
        verify(foundItemRepository).findAll();
    }


    @Test
    void deletePhoto_WhenUserIsOwner_RemovesPhoto() throws Exception {
        String validToken = "Bearer token";

        when(request.getHeader("Authorization")).thenReturn(validToken);
        when(jwtGenerator.validateToken("token")).thenReturn(true);
        when(jwtGenerator.getUsernameFromJWT("token")).thenReturn("user");

        UserEntity mockUser = new UserEntity();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(mockUser));

        UUID itemId = UUID.randomUUID();
        FoundItem mockItem = new FoundItem();
        mockItem.setId(itemId);
        mockItem.setUser(mockUser);
        UUID id = UUID.randomUUID();
        mockItem.setPhotoId(id);
        when(foundItemRepository.findById(itemId)).thenReturn(Optional.of(mockItem));

        doNothing().when(itemPhotoService).deletePhotoById(mockItem.getPhotoId());

        foundItemService.deletePhoto(itemId, request);

        assertNull(mockItem.getPhotoId());
        verify(foundItemRepository).save(mockItem);
        verify(itemPhotoService).deletePhotoById(id);
    }

    @Test
    void deletePhoto_WhenAdminDeletes_RemovesOtherUsersPhoto() throws Exception {
        String validToken = "Bearer token";

        when(request.getHeader("Authorization")).thenReturn(validToken);
        when(jwtGenerator.validateToken("token")).thenReturn(true);

        UserEntity admin = new UserEntity();
        admin.setId(UUID.randomUUID());
        admin.setUsername("admin");

        when(jwtGenerator.getUsernameFromJWT("token")).thenReturn("admin");

        UserEntity mockUser = new UserEntity();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(mockUser));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        List<String> roles = List.of("ADMIN");
        when(jwtGenerator.getRolesFromJWT("token")).thenReturn(roles);

        UUID itemId = UUID.randomUUID();
        FoundItem mockItem = new FoundItem();
        mockItem.setId(itemId);
        mockItem.setUser(mockUser);
        UUID id = UUID.randomUUID();
        mockItem.setPhotoId(id);
        when(foundItemRepository.findById(itemId)).thenReturn(Optional.of(mockItem));


        doNothing().when(itemPhotoService).deletePhotoById(mockItem.getPhotoId());

        foundItemService.deletePhoto(itemId, request);

        assertNull(mockItem.getPhotoId());
        verify(foundItemRepository).save(mockItem);
        verify(itemPhotoService).deletePhotoById(id);
    }

}
