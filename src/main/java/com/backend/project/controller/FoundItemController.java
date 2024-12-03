package com.backend.project.controller;

import com.backend.project.dto.FoundItemDto;
import com.backend.project.exceptions.ItemNotFoundException;
import com.backend.project.model.FoundItem;
import com.backend.project.model.FoundItem;
import com.backend.project.model.Office;
import com.backend.project.model.UserEntity;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import com.backend.project.dto.FoundItemDto;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FoundItemController {

    private final FoundItemService itemService;
    private final JWTGenerator jwtGenerator;
    private final UserService userService;

    @Autowired
    public FoundItemController(FoundItemService service, JWTGenerator jwtGenerator, UserService userService) {
        this.itemService = service;
        this.jwtGenerator = jwtGenerator;
        this.userService = userService;
    }

    @GetMapping("/found_items")
    public ResponseEntity<List<FoundItem>> getItems() {
        List<FoundItem> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/found_items/{id}")
    public ResponseEntity<FoundItem> getItemsById(@PathVariable UUID id) {
        try {
            FoundItem item = itemService.getItemById(id);
            return ResponseEntity.ok(item);
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/found_items/submit")
    public ResponseEntity<String> addItem(
            @RequestBody @Valid FoundItemDto itemDto,
            HttpServletRequest request) {

        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token != null && jwtGenerator.validateToken(token)) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            UserEntity user = userService.getUserByUsername(username);

            if (user != null) {
                try {
                    if (itemDto.photo() == null || itemDto.photo().isEmpty()) {
                        return new ResponseEntity<>("No photo provided", HttpStatus.BAD_REQUEST);
                    }

                    byte[] photoBytes = Base64.getDecoder().decode(itemDto.photo());

                    String fileName = username + "_" + System.currentTimeMillis() + ".jpg";
                    Path filePath = Paths.get("src/main/resources/items", fileName);

                    Files.createDirectories(filePath.getParent());
                    Files.write(filePath, photoBytes);

                    FoundItemDto updatedItemDto = new FoundItemDto(
                            itemDto.name(),
                            itemDto.category(),
                            itemDto.description(),
                            itemDto.office(),
                            filePath.toString(),
                            itemDto.foundDate(),
                            itemDto.foundPlace()
                    );
                    System.out.println(filePath.toString());
                    String id = itemService.addItem(updatedItemDto, user).getId().toString();

                    String fileUrl = "/items/" + fileName;
                    return new ResponseEntity<>(fileUrl, HttpStatus.OK);

                } catch (IOException e) {
                    e.printStackTrace();
                    return new ResponseEntity<>("Error saving photo", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @DeleteMapping("/found_items/{id}")
    public void deleteItemById(@PathVariable UUID id){
        itemService.removeItem(id);
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


}


