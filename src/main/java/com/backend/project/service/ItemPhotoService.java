package com.backend.project.service;

import com.backend.project.exceptions.*;
import com.backend.project.model.PhotoItem;
import com.backend.project.repository.ItemPhotoRepository;
import com.backend.project.storage.FileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;


@Service
public class ItemPhotoService {

    private final ItemPhotoRepository itemPhotoRepository;
    private FileStorage fileStorage;

    @Autowired
    public ItemPhotoService(ItemPhotoRepository itemPhotoRepository, FileStorage fileStorage){
        this.itemPhotoRepository = itemPhotoRepository;
        this.fileStorage = fileStorage;
    }

    public PhotoItem addPhoto(MultipartFile content) throws FailedUploadingPhoto {
        if(!Objects.equals(content.getContentType(), "image/png") && !Objects.equals(content.getContentType(), "image/jpeg") && !Objects.equals(content.getContentType(), "image/gif")){
            throw new FailedUploadingPhoto("Unsupported file format");
        }

        String externalId;
        try {
            externalId = fileStorage.upload(content);
        }catch(FileException e){
            throw new FailedUploadingPhoto(e.getMessage());
        }

        PhotoItem photoItem = new PhotoItem(externalId);
        photoItem.setContentType(content.getContentType());
        photoItem.setName(content.getOriginalFilename());
        return itemPhotoRepository.save(photoItem);
    }

    public void deletePhotoFromBoth(PhotoItem photo){
        fileStorage.deleteFile(photo.getExternalId());
        itemPhotoRepository.deleteById(photo.getId());
    }

    public void deletePhotoById(UUID id){
        PhotoItem photo = getPhotoById(id);
        if(photo != null) {
            deletePhotoFromBoth(photo);
        }
    }

    public Resource asResource(PhotoItem photo) {
        InputStream stream = fileStorage.download(photo.getExternalId());
        return new InputStreamResource(stream);
    }


    public PhotoItem getPhotoById(UUID photoId) {
        return itemPhotoRepository.findById(photoId)
                .orElse(null);
    }
}

