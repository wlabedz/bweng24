package com.backend.project.controller;

import com.backend.project.dto.FoundItemDto;
import com.backend.project.dto.FoundItemRetDto;
import com.backend.project.dto.OfficeDto;
import com.backend.project.exceptions.*;
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
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<List<FoundItemRetDto>> getItems() {
        List<FoundItemRetDto> items = itemService.getAllItems().orElse(null);
        return ResponseEntity.ok(items);
    }

    @GetMapping("added_found_items")
    public ResponseEntity<List<FoundItemRetDto>> getItemsAddedByUser(HttpServletRequest request){
        try {
            List<FoundItemRetDto> items = itemService.getItemsByUser(request).orElse(null);
            return ResponseEntity.ok(items);
        }catch(InvalidToken|UserNotFoundException exception){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @GetMapping("/found_items/{id}")
    public ResponseEntity<FoundItemRetDto> getItemById(@PathVariable UUID id) {
        try {
            FoundItemRetDto item = itemService.getItemById(id);
            return ResponseEntity.ok(item);
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/found_items/submit")
    public ResponseEntity<Void> addItem(HttpServletRequest request, @RequestPart("dto") @Valid FoundItemDto itemDto, @RequestPart("file") MultipartFile file) {
        String id;
        try {
            id = itemService.addItem(itemDto, request, file).getId().toString();

            return ResponseEntity
                    .created(URI.create("/offices/" + id))
                    .build();
        } catch (InvalidToken | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (FailedUploadingPhoto e) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(null);
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

    @DeleteMapping("/found_items/{id}/picture")
    public ResponseEntity<Void> deletePicture(@PathVariable UUID id, HttpServletRequest request){
        try {
            itemService.deletePhoto(id, request);
            return ResponseEntity.ok().build();
        }catch(InvalidToken | UserNotFoundException | NotAllowedException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @PutMapping("/found_items/{id}/picture")
    public ResponseEntity<FoundItemRetDto> uploadNewPicture(@PathVariable UUID id, @RequestParam("photo") MultipartFile photo, HttpServletRequest request){
        try{
            FoundItemRetDto item = itemService.uploadNewPicture(id, photo, request);
            return ResponseEntity.ok(item);
        }catch(InvalidToken | UserNotFoundException | NotAllowedException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }catch(FailedUploadingPhoto e){
            return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }


    @PutMapping("/found_items/{id}")
    public ResponseEntity<FoundItemRetDto> updateItem(@PathVariable UUID id, @RequestBody @Valid FoundItemDto dto, HttpServletRequest request) {
        try {
            FoundItemRetDto updatedItem = itemService.updateItem(id, dto, request);
            return ResponseEntity.ok(updatedItem);
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (InvalidToken | UserNotFoundException | NotAllowedException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/found_items/search")
    public ResponseEntity<List<FoundItemRetDto>> searchFoundItems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false)  LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        List<FoundItemRetDto> items = itemService.searchItems(category, name, userId, startDate, endDate).orElse(null);
        return ResponseEntity.ok(items);
    }


}


