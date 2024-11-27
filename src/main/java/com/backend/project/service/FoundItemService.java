package com.backend.project.service;

import com.backend.project.dto.FoundItemDto;
import com.backend.project.exceptions.ItemNotFoundException;
import com.backend.project.model.FoundItem;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.FoundItemRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class FoundItemService {

    private FoundItemRepository itemRepository;
    private static final String IMAGE_DIRECTORY = "/src/main/resources/items";

    public FoundItemService(FoundItemRepository repository){
        this.itemRepository = repository;
    }


    public List<FoundItem> getAllItems() {
        return itemRepository.findAll().stream()
                .peek(item -> item.setPhoto(getFullPhotoPath(item.getPhoto())))
                .collect(Collectors.toList());
    }

    public FoundItem getItemById(UUID id) {
        FoundItem item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        item.setPhoto(getFullPhotoPath(item.getPhoto()));
        return item;
    }

    public FoundItem addItem(FoundItemDto itemDto, UserEntity user) {
        FoundItem item;

            item = new FoundItem(user,itemDto.name(), itemDto.category(), itemDto.description(), itemDto.office(), itemDto.photo(), itemDto.foundDate(), itemDto.foundPlace());

            itemRepository.save(item);
        return itemRepository.save(item);
    }

    public void removeItem(UUID id) {
        FoundItem itemToDelete = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        itemRepository.deleteById(itemToDelete.getId());
    }


    public FoundItem updateItemDescription(UUID id, String newDescription) {
        FoundItem existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        existingItem.setDescription(newDescription);
        return itemRepository.save(existingItem);
    }

    private String getFullPhotoPath(String photoFilename) {
        Path path = Paths.get(photoFilename);
        try {
            byte[] photoBytes = Files.readAllBytes(path);
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(photoBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
