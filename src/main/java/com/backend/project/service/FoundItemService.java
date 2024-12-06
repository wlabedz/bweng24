package com.backend.project.service;

import com.backend.project.dto.FoundItemDto;
import com.backend.project.exceptions.*;
import com.backend.project.model.*;
import com.backend.project.repository.FoundItemRepository;
import com.backend.project.repository.UserRepository;
import com.backend.project.service.ItemPhotoService;
import com.backend.project.security.JWTGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
import com.backend.project.exceptions.InvalidToken;
import com.backend.project.exceptions.UserNotFoundException;
import com.backend.project.model.UserEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
import java.util.Optional;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class FoundItemService {

    private final FoundItemRepository foundItemRepository;
    private final UserRepository userRepository;
    private final ItemPhotoService itemPhotoService;
    private final JWTGenerator jwtGenerator;

    @Autowired
    public FoundItemService(FoundItemRepository foundItemRepository,
                         UserRepository userRepository,
                         ItemPhotoService itemPhotoService,
                            JWTGenerator jwtGenerator) {
        this.foundItemRepository = foundItemRepository;
        this.userRepository = userRepository;
        this.itemPhotoService = itemPhotoService;
        this.jwtGenerator = jwtGenerator;
    }

    public FoundItem addItem(FoundItemDto foundItemDto, HttpServletRequest request) throws InvalidToken, UserNotFoundException, FailedUploadingPhoto {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            throw new InvalidToken("Token body does not comply with assumed format and therefore cannot be validated");
        }

        if (jwtGenerator.validateToken(token)) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            UserEntity user = userRepository.findByUsername(username).orElse(null);

            if (user == null) {
                throw new UserNotFoundException("User could not have been found");
            }

            ItemPhoto itemPhoto = itemPhotoService.addPhoto(foundItemDto.photo());
            FoundItem newItem = new FoundItem(
                    user,
                    foundItemDto.name(),
                    foundItemDto.category(),
                    foundItemDto.description(),
                    foundItemDto.office(),
                    itemPhoto.getId(),
                    foundItemDto.foundDate(),
                    foundItemDto.foundPlace()
                    );

            return foundItemRepository.save(newItem);
        } else {
            throw new InvalidToken("Token cannot be validated");
        }
    }

    public FoundItem updateItemDescription(UUID id, String newDescription) {
        FoundItem existingItem = foundItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        existingItem.setDescription(newDescription);

        return foundItemRepository.save(existingItem);
    }

    public FoundItemDto getItemById(UUID itemId) {
        FoundItem foundItem = foundItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        String photoContent = foundItem.getPhotoId() != null
                ? itemPhotoService.getPhotoById(foundItem.getPhotoId()).getContent()
                : null;

        return new FoundItemDto(
                foundItem.getId(),
                foundItem.getName(),
                foundItem.getCategory(),
                foundItem.getDescription(),
                foundItem.getOffice(),
                photoContent,
                foundItem.getFoundDate(),
                foundItem.getFoundPlace()
        );
    }

    public List<FoundItemDto> getAllItems() {
        List<FoundItem> items = foundItemRepository.findAll();

        return items.stream()
                .map(item -> new FoundItemDto(
                        item.getId(),
                        item.getName(),
                        item.getCategory(),
                        item.getDescription(),
                        item.getOffice(),
                        item.getPhotoId() != null
                                ? itemPhotoService.getPhotoById(item.getPhotoId()).getContent()
                                : null,
                        item.getFoundDate(),
                        item.getFoundPlace()
                ))
                .collect(Collectors.toList());
    }

    public void deleteItem(UUID itemId) throws InvalidToken{
        FoundItem item = foundItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        foundItemRepository.deleteById(item.getId());
    }

    public List<FoundItemDto> searchItems(String category, String name, UUID userId, LocalDate startDate, LocalDate endDate) {
        List<FoundItem> items = foundItemRepository.findAll().stream()
                .filter(item -> category == null || item.getCategory().equalsIgnoreCase(category))
                .filter(item -> name == null || item.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(item -> userId == null || (item.getUser() != null && item.getUser().getId().equals(userId)))
                .filter(item -> startDate == null || !item.getFoundDate().isBefore(startDate))
                .filter(item -> endDate == null || !item.getFoundDate().isAfter(endDate))
                .toList();

        return items.stream()
                .map(item -> new FoundItemDto(
                        item.getId(),
                        item.getName(),
                        item.getCategory(),
                        item.getDescription(),
                        item.getOffice(),
                        itemPhotoService.getPhotoById(item.getPhotoId()).getContent(),
                        item.getFoundDate(),
                        item.getFoundPlace()
                ))
                .collect(Collectors.toList());
    }
}
