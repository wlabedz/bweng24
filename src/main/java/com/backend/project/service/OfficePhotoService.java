package com.backend.project.service;

import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.exceptions.FileException;
import com.backend.project.model.PhotoOffice;
import com.backend.project.repository.OfficePhotoRepository;
import com.backend.project.storage.FileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class OfficePhotoService {
    private final OfficePhotoRepository officePhotoRepository;
    private final FileStorage fileStorage;

    @Autowired
    public OfficePhotoService(OfficePhotoRepository officePhotoRepository, FileStorage fileStorage){
        this.officePhotoRepository = officePhotoRepository;
        this.fileStorage = fileStorage;
    }


    public PhotoOffice addPhoto(MultipartFile content) throws FailedUploadingPhoto {
        if(!Objects.equals(content.getContentType(), "image/png") && !Objects.equals(content.getContentType(), "image/jpeg") && !Objects.equals(content.getContentType(), "image/gif")){
            throw new FailedUploadingPhoto("Unsupported file format");
        }
        String externalId;
        try {
            externalId = fileStorage.upload(content);
        }catch(FileException e){
            throw new FailedUploadingPhoto(e.getMessage());
        }

        PhotoOffice photoOffice = new PhotoOffice(externalId);
        photoOffice.setContentType(content.getContentType());
        photoOffice.setName(content.getOriginalFilename());
        return officePhotoRepository.save(photoOffice);
    }

    public Optional<PhotoOffice> getPhotoById(UUID id){
        return officePhotoRepository.findById(id);
    }

    public void deletePhotoFromBoth(PhotoOffice photo){
        fileStorage.deleteFile(photo.getExternalId());
        officePhotoRepository.deleteById(photo.getId());
    }

    public void deletePhotoById(UUID id){
        PhotoOffice photo = getPhotoById(id).orElse(null);
        if(photo != null){
            deletePhotoFromBoth(photo);
        }
    }

    public Resource asResource(PhotoOffice photo) {
        InputStream stream = fileStorage.download(photo.getExternalId());
        return new InputStreamResource(stream);
    }

}
