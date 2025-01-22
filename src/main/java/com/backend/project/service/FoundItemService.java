package com.backend.project.service;

import com.backend.project.dto.DistrictDto;
import com.backend.project.dto.FoundItemDto;
import com.backend.project.dto.FoundItemRetDto;
import com.backend.project.dto.OfficeRetDto;
import com.backend.project.exceptions.*;
import com.backend.project.model.*;
import com.backend.project.repository.FoundItemRepository;
import com.backend.project.repository.UserRepository;
import com.backend.project.service.ItemPhotoService;
import com.backend.project.security.JWTGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
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
import org.springframework.web.multipart.MultipartFile;

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

    public FoundItem addItem(FoundItemDto foundItemDto, HttpServletRequest request, MultipartFile file) throws InvalidToken, UserNotFoundException, FailedUploadingPhoto {
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

            PhotoItem photoItem;
            try{
                photoItem = itemPhotoService.addPhoto(file);
            }catch(FailedUploadingPhoto ex){
                throw new FailedUploadingPhoto(ex.getMessage());
            }

            FoundItem newItem = new FoundItem(
                    user,
                    foundItemDto.name(),
                    foundItemDto.category(),
                    foundItemDto.description(),
                    foundItemDto.office(),
                    photoItem.getId(),
                    foundItemDto.foundDate(),
                    foundItemDto.foundPlace()
                    );

            return foundItemRepository.save(newItem);
        } else {
            throw new InvalidToken("Token cannot be validated");
        }
    }

    public Optional<List<FoundItemRetDto>> getItemsByUser(HttpServletRequest request) throws InvalidToken {
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
            List<FoundItemRetDto> items = foundItemRepository.findAll().stream().filter(item -> item.getUser().getId().equals(user.getId())).map(this::mapToDto).toList();
            return items.isEmpty() ? Optional.empty() : Optional.of(items);
        } else {
            throw new InvalidToken("Token cannot be validated");
        }
    }


    public FoundItemRetDto uploadNewPicture(UUID id, MultipartFile photo, HttpServletRequest request) throws FailedUploadingPhoto, InvalidToken, NotAllowedException {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

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

            List<String> roles = jwtGenerator.getRolesFromJWT(token);

            if (!roles.contains("ADMIN") && !user.getId().equals(item.getUser().getId())) {
                throw new NotAllowedException(user.getUsername());
            }

            try{
                if(item.getPhotoId() != null){
                    itemPhotoService.deletePhotoById(item.getPhotoId());
                }
                PhotoItem addedPhoto = itemPhotoService.addPhoto(photo);
                item.setUpdatedAt(LocalDateTime.now());
                item.setPhotoId(addedPhoto.getId());
                System.out.println(addedPhoto.getId());
                return mapToDto(foundItemRepository.save(item));
            }catch(FailedUploadingPhoto e){
                throw new FailedUploadingPhoto("Failed uploading the picture");
            }
        }
        else {
            throw new InvalidToken("Token cannot be validated");
        }
    }


    public void deletePhoto(UUID id, HttpServletRequest request) throws InvalidToken, NotAllowedException {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

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

            List<String> roles = jwtGenerator.getRolesFromJWT(token);

            if (!roles.contains("ADMIN") && !user.getId().equals(item.getUser().getId())) {
                throw new NotAllowedException(user.getUsername());
            }

            if (item != null) {
                if (item.getPhotoId() != null) {
                    itemPhotoService.deletePhotoById(item.getPhotoId());
                    item.setPhotoId(null);
                    item.setUpdatedAt(LocalDateTime.now());
                    foundItemRepository.save(item);
                }
            }
        }
        else {
                throw new InvalidToken("Token cannot be validated");
            }
        }



    public FoundItemRetDto updateItem(UUID id, FoundItemDto dto, HttpServletRequest request) throws InvalidToken, NotAllowedException {

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

            List<String> roles = jwtGenerator.getRolesFromJWT(token);

            FoundItem existingItem = foundItemRepository.findById(id)
                    .orElseThrow(() -> new ItemNotFoundException(id));


            if (!roles.contains("ADMIN") && !user.getId().equals(existingItem.getUser().getId())) {
                throw new NotAllowedException(user.getUsername());
            }

            existingItem.setDescription(dto.description());
            existingItem.setCategory(dto.category());
            existingItem.setFoundDate(dto.foundDate());
            existingItem.setFoundPlace(dto.foundPlace());
            existingItem.setUpdatedAt(LocalDateTime.now());

            return mapToDto(foundItemRepository.save(existingItem));
        } else {
            throw new InvalidToken("Token cannot be validated");
        }
    }

    public FoundItemRetDto getItemById(UUID itemId) {
        return foundItemRepository.findAll()
                .stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst().map(this::mapToDto).orElse(null);
    }

    public Optional<List<FoundItemRetDto>> getAllItems() {
        List<FoundItemRetDto> items = foundItemRepository.findAll()
                .stream()
                .map(this::mapToDto).toList();

        return items.isEmpty() ? Optional.empty() : Optional.of(items);
    }

    public void deleteItem(UUID itemId) throws InvalidToken{
        FoundItem item = foundItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        if(item.getPhotoId() != null){
            itemPhotoService.deletePhotoById(item.getPhotoId());
        }
        foundItemRepository.deleteById(item.getId());
    }

    public Optional<List<FoundItemRetDto>> searchItems(String category, String name, UUID userId, LocalDate startDate, LocalDate endDate) {
        List<FoundItemRetDto> items = foundItemRepository.findAll().stream()
                .filter(item -> category == null || item.getCategory().equalsIgnoreCase(category))
                .filter(item -> name == null || item.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(item -> userId == null || (item.getUser() != null && item.getUser().getId().equals(userId)))
                .filter(item -> startDate == null || !item.getFoundDate().isBefore(startDate))
                .filter(item -> endDate == null || !item.getFoundDate().isAfter(endDate))
                .map(this::mapToDto).toList();

        return items.isEmpty() ? Optional.empty() : Optional.of(items);
    }


    private FoundItemRetDto mapToDto(FoundItem foundItem) {
        PhotoItem photoItem = null;
        if(foundItem.getPhotoId() != null){
            photoItem = itemPhotoService.getPhotoById(foundItem.getPhotoId());
        }

        String content;
        if(photoItem != null){
            try{
                Resource photo = itemPhotoService.asResource(photoItem);
                byte[] imageBytes = photo.getContentAsByteArray();
                content = Base64.getEncoder().encodeToString(imageBytes);
            }catch(Exception e){
                throw new FileException("Cannot load user picture",e);
            }
        }else{
            content = null;
        }

        return new FoundItemRetDto(
                foundItem.getId(),
                foundItem.getUser(),
                foundItem.getName(),
                foundItem.getCategory(),
                foundItem.getDescription(),
                foundItem.getOffice(),
                content,
                foundItem.getFoundDate(),
                foundItem.getFoundPlace()
        );
    }
}
