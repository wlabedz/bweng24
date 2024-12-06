package com.backend.project.controller;

import com.backend.project.dto.FoundItemDto;
import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.exceptions.ItemNotFoundException;
import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.model.*;
import com.backend.project.model.FoundItem;
import com.backend.project.security.JWTGenerator;
import com.backend.project.service.FoundItemService;
import com.backend.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Base64;
import com.backend.project.dto.FoundItemDto;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FoundItemController {

    private final FoundItemService itemService;

    @Autowired
    public FoundItemController(FoundItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/found_items")
    public ResponseEntity<List<FoundItemDto>> getItems() {
        List<FoundItemDto> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/found_items/{id}")
    public ResponseEntity<FoundItemDto> getItemById(@PathVariable UUID id) {
        try {
            FoundItemDto item = itemService.getItemById(id);
            return ResponseEntity.ok(item);
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/found_items/submit")
    public ResponseEntity<Void> addItem(HttpServletRequest request, @RequestBody @Valid FoundItemDto itemDto) {
        try {
            FoundItem newItem = itemService.addItem(itemDto, request);

            return ResponseEntity.created(URI.create("/found_items/" + newItem.getId())).build();
        } catch (InvalidToken | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (FailedUploadingPhoto e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/found_items/{id}")
    public ResponseEntity<Void> deleteItemById(@PathVariable UUID id) {
        try {
            itemService.deleteItem(id);
            return ResponseEntity.noContent().build();
        } catch (InvalidToken e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/found_items/description/{id}")
    public ResponseEntity<FoundItem> updateItemDescription(@PathVariable UUID id, @RequestBody FoundItemDto dto) {
        try {
            FoundItem updatedItem = itemService.updateItemDescription(id, dto.description());
            return ResponseEntity.ok(updatedItem);
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/found_items/search")
    public ResponseEntity<List<FoundItemDto>> searchFoundItems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false)  LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        List<FoundItemDto> items = itemService.searchItems(category, name, userId, startDate, endDate);
        return ResponseEntity.ok(items);
    }


}


