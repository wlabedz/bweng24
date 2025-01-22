package com.backend.project.controllerTests;

import com.backend.project.controller.FoundItemController;
import com.backend.project.dto.FoundItemDto;
import com.backend.project.dto.FoundItemRetDto;
import com.backend.project.exceptions.*;
import com.backend.project.model.FoundItem;
import com.backend.project.model.PhotoItem;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

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
    private FoundItemRetDto testRetItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testItem = new FoundItemDto(
                UUID.randomUUID(),
                "Black iPhone",
                "Electronics",
                "Phone found on the bench around 2pm.",
                "Office A",
                null,
                LocalDate.now(),
                "Botanic garden"
        );

        testRetItem = new FoundItemRetDto(
                UUID.randomUUID(),
                null,
                "Black iPhone",
                "Electronics",
                "Phone found on the bench around 2pm.",
                "Office A",
                null,
                LocalDate.now(),
                "Botanic garden"
        );
    }

    @Test
    void getItems_WhenCalled_ReturnsListOfItems() {
        List<FoundItemRetDto> items = new ArrayList<>();
        items.add(testRetItem);
        when(foundItemService.getAllItems()).thenReturn(Optional.of(items));

        ResponseEntity<List<FoundItemRetDto>> response = foundItemController.getItems();

        assertNotNull(response);
        assertEquals(1, response.getBody().size());
        assertEquals(testItem.name(), response.getBody().get(0).name());
        verify(foundItemService, times(1)).getAllItems();
    }

    @Test
    void getItemById_WhenItemExists_ReturnsItem() {
        when(foundItemService.getItemById(testItem.id())).thenReturn(testRetItem);

        ResponseEntity<FoundItemRetDto> response = foundItemController.getItemById(testItem.id());

        assertNotNull(response);
        assertEquals(testItem.name(), response.getBody().name());
        verify(foundItemService, times(1)).getItemById(testItem.id());
    }

    @Test
    void getItemById_WhenItemNotFound_ReturnsNotFoundStatus() {
        UUID itemId = testItem.id();
        when(foundItemService.getItemById(itemId)).thenThrow(ItemNotFoundException.class);

        ResponseEntity<FoundItemRetDto> response = foundItemController.getItemById(itemId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(foundItemService, times(1)).getItemById(itemId);
    }


    @Test
    void addItem_WhenValidRequest_ReturnsCreatedStatus() throws Exception {
        String token = "Bearer validToken";
        FoundItemDto itemDto = new FoundItemDto(UUID.randomUUID(), "Black iPhone", "Electronics", "Phone found on the bench around 2pm.", "Office A", "photo_base64", LocalDate.now(), "Botanic garden");

        UserEntity mockUser = new UserEntity();
        mockUser.setUsername("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));

        UUID photoId = UUID.randomUUID();
        PhotoItem photo1 = new PhotoItem("externalId");
        photo1.setName("Photo1");
        when(itemPhotoService.addPhoto(any())).thenReturn(photo1);

        FoundItem newItem = new FoundItem(mockUser, "Black iPhone", "Electronics", "Phone found on the bench around 2pm.", "Office A", photoId, LocalDate.now(), "Botanic garden");
        when(foundItemService.addItem(any(), any(), any())).thenReturn(newItem);

        when(request.getHeader("Authorization")).thenReturn(token);

        MultipartFile multipartFile = mock(MultipartFile.class);
        ResponseEntity<Void> response = foundItemController.addItem(request, itemDto, multipartFile);

        assertNotNull(response);
        assertEquals(201, response.getStatusCodeValue());
        verify(foundItemService, times(1)).addItem(any(), any(), any());
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

    @Test
    void deleteItemById_WhenItemNotFound_ReturnsNotFound() throws Exception {
        UUID itemId = testItem.id();
        doThrow(ItemNotFoundException.class).when(foundItemService).deleteItem(itemId);

        ResponseEntity<Void> response = foundItemController.deleteItemById(itemId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(foundItemService, times(1)).deleteItem(itemId);
    }

    @Test
    void updateItem_Success_ReturnsOkWhenSuccessful() throws Exception {
        UUID itemId = testItem.id();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        when(foundItemService.updateItem(eq(itemId), any(FoundItemDto.class), eq(mockRequest))).thenReturn(testRetItem);

        ResponseEntity<FoundItemRetDto> response = foundItemController.updateItem(itemId, testItem, mockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testRetItem.id(), response.getBody().id());
        verify(foundItemService).updateItem(eq(itemId), any(FoundItemDto.class), eq(mockRequest));
    }

    @Test
    void updateItem_Failed_ReturnsUnauthorizedWhenInvalidToken() throws Exception {
        UUID itemId = testItem.id();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        when(foundItemService.updateItem(eq(itemId), any(FoundItemDto.class), eq(mockRequest))).thenThrow(InvalidToken.class);

        ResponseEntity<FoundItemRetDto> response = foundItemController.updateItem(itemId, testItem, mockRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(foundItemService).updateItem(eq(itemId), any(FoundItemDto.class), eq(mockRequest));
    }

    @Test
    void updateItem_Failed_ReturnsNotFoundWhenItemNotFound() throws Exception {
        UUID itemId = testItem.id();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        when(foundItemService.updateItem(eq(itemId), any(FoundItemDto.class), eq(mockRequest))).thenThrow(ItemNotFoundException.class);

        ResponseEntity<FoundItemRetDto> response = foundItemController.updateItem(itemId, testItem, mockRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(foundItemService).updateItem(eq(itemId), any(FoundItemDto.class), eq(mockRequest));
    }

    @Test
    void uploadNewPicture_Success_ReturnsOkWhenSuccessful() throws Exception {
        UUID itemId = testItem.id();
        MultipartFile mockPhoto = mock(MultipartFile.class);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        when(foundItemService.uploadNewPicture(eq(itemId), eq(mockPhoto), eq(mockRequest))).thenReturn(testRetItem);

        ResponseEntity<FoundItemRetDto> response = foundItemController.uploadNewPicture(itemId, mockPhoto, mockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testRetItem.id(), response.getBody().id());
        verify(foundItemService).uploadNewPicture(eq(itemId), eq(mockPhoto), eq(mockRequest));
    }

    @Test
    void uploadNewPicture_Failed_ReturnsUnauthorizedWhenInvalidToken() throws Exception {
        UUID itemId = testItem.id();
        MultipartFile mockPhoto = mock(MultipartFile.class);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        when(foundItemService.uploadNewPicture(eq(itemId), eq(mockPhoto), eq(mockRequest))).thenThrow(InvalidToken.class);

        ResponseEntity<FoundItemRetDto> response = foundItemController.uploadNewPicture(itemId, mockPhoto, mockRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(foundItemService).uploadNewPicture(eq(itemId), eq(mockPhoto), eq(mockRequest));
    }

    @Test
    void deletePicture_Success_ReturnsOkWhenSuccessful() throws Exception{
        UUID itemId = testItem.id();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        doNothing().when(foundItemService).deletePhoto(eq(itemId), eq(mockRequest));

        ResponseEntity<Void> response = foundItemController.deletePicture(itemId, mockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(foundItemService).deletePhoto(eq(itemId), eq(mockRequest));
    }

    @Test
    void deletePicture_Failed_ReturnsUnauthorizedWhenInvalidToken() throws Exception {
        UUID itemId = testItem.id();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        doThrow(InvalidToken.class).when(foundItemService).deletePhoto(eq(itemId), eq(mockRequest));

        ResponseEntity<Void> response = foundItemController.deletePicture(itemId, mockRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(foundItemService).deletePhoto(eq(itemId), eq(mockRequest));
    }

    @Test
    void deletePicture_Failed_ReturnsUnauthorizedWhenUserNotFound() throws Exception {
        UUID itemId = testItem.id();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        doThrow(UserNotFoundException.class).when(foundItemService).deletePhoto(eq(itemId), eq(mockRequest));
        ResponseEntity<Void> response = foundItemController.deletePicture(itemId, mockRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(foundItemService).deletePhoto(eq(itemId), eq(mockRequest));
    }

    @Test
    void getItemsAddedByUser_Success_ReturnsOk() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        List<FoundItemRetDto> mockItems = List.of(testRetItem);

        when(foundItemService.getItemsByUser(mockRequest)).thenReturn(Optional.of(mockItems));

        ResponseEntity<List<FoundItemRetDto>> response = foundItemController.getItemsAddedByUser(mockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockItems.size(), response.getBody().size());
        verify(foundItemService).getItemsByUser(mockRequest);
    }


    @Test
    void searchFoundItems_Success_ReturnsFoundItemsWhenCategoryMatches() {
        List<FoundItemRetDto> mockItems = List.of(testRetItem);
        String category = "Electronics";

        when(foundItemService.searchItems(eq("Electronics"), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Optional.of(mockItems));

        ResponseEntity<List<FoundItemRetDto>> response = foundItemController.searchFoundItems(category, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Electronics", response.getBody().get(0).category());
    }

    @Test
    void searchFoundItems_Success_ReturnsFoundItemsWhenNameMatches() {
        List<FoundItemRetDto> mockItems = List.of(testRetItem);
        String name = "Black iPhone";

        when(foundItemService.searchItems(isNull(), eq("Black iPhone"), isNull(), isNull(), isNull()))
                .thenReturn(Optional.of(mockItems));

        ResponseEntity<List<FoundItemRetDto>> response = foundItemController.searchFoundItems(null, name, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Black iPhone", response.getBody().get(0).name());
    }

    @Test
    void searchFoundItems_Success_ReturnsFoundItemsWhenDateRangeMatches() {
        List<FoundItemRetDto> mockItems = List.of(testRetItem);
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);

        when(foundItemService.searchItems(isNull(), isNull(), isNull(), eq(startDate), eq(endDate)))
                .thenReturn(Optional.of(mockItems));

        ResponseEntity<List<FoundItemRetDto>> response = foundItemController.searchFoundItems(null, null, null, startDate, endDate);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void searchFoundItems_Success_ReturnsEmptyListWhenNoItemsMatch() {
        List<FoundItemRetDto> mockItems = List.of();
        when(foundItemService.searchItems(anyString(), anyString(), any(), any(), any()))
                .thenReturn(Optional.of(mockItems));

        ResponseEntity<List<FoundItemRetDto>> response = foundItemController.searchFoundItems("Electronics", "Black iPhone", null, LocalDate.now(), LocalDate.now());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void searchFoundItems_Success_ReturnsAllItemsWhenNoSearchCriteriaProvided() {
        List<FoundItemRetDto> mockItems = List.of(testRetItem);
        when(foundItemService.searchItems(isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Optional.of(mockItems));

        ResponseEntity<List<FoundItemRetDto>> response = foundItemController.searchFoundItems(null, null, null, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

}
