package com.backend.project.service;

import com.backend.project.model.OfficePhoto;
import com.backend.project.repository.OfficePhotoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OfficePhotoService {
    private OfficePhotoRepository officePhotoRepository;

    public OfficePhotoService(OfficePhotoRepository officePhotoRepository){
        this.officePhotoRepository = officePhotoRepository;
    }


    public OfficePhoto addPhoto(String content){
        OfficePhoto usph = new OfficePhoto(content);

        return officePhotoRepository.save(usph);
    }

    public Optional<OfficePhoto> getPhotoById(UUID id){
        return officePhotoRepository.findById(id);
    }

    public void deletePhotoById(UUID id){
        officePhotoRepository.deleteById(id);
    }
}
