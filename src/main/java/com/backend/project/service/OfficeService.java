package com.backend.project.service;

import com.backend.project.dto.DistrictDto;
import com.backend.project.dto.OfficeDto;
import com.backend.project.dto.OfficeRetDto;
import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.exceptions.FileException;
import com.backend.project.exceptions.OfficeNotFoundException;
import com.backend.project.model.District;
import com.backend.project.model.Office;
import com.backend.project.model.PhotoOffice;
import com.backend.project.repository.DistrictRepository;
import com.backend.project.repository.OfficeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OfficeService {

    private final OfficeRepository officeRepository;
    private final DistrictRepository districtRepository;
    private final OfficePhotoService officePhotoService;

    @Autowired
    public OfficeService(OfficeRepository officeRepository,
                       DistrictRepository districtRepository,
                        OfficePhotoService officePhotoService) {
        this.officeRepository = officeRepository;
        this.districtRepository = districtRepository;
        this.officePhotoService = officePhotoService;
    }

    public Optional<List<OfficeRetDto>> getAllOffices() {
        List<OfficeRetDto> off =  officeRepository.findAll().stream().map(this::mapToDto).toList();
        return off.isEmpty() ? Optional.empty() : Optional.of(off);
    }

    public Office addOffice(OfficeDto officeDto, MultipartFile file) throws FailedUploadingPhoto {
        Optional<District> existingDistrict = districtRepository.findById(officeDto.district().id());
        District district;
        if (existingDistrict.isPresent()) {
            district = existingDistrict.get();
        } else {
            district = new District(officeDto.district().id(), officeDto.district().name());
            districtRepository.save(district);
        }

        PhotoOffice photoOffice;
        try{
            photoOffice = officePhotoService.addPhoto(file);
        }catch(FailedUploadingPhoto ex){
            throw new FailedUploadingPhoto(ex.getMessage());
        }

        Office office = new Office(district, officeDto.phoneNumber(), officeDto.address(),
                photoOffice.getId(), officeDto.description());

        return officeRepository.save(office);
    }

    public OfficeRetDto getOfficeById(String id){
        return officeRepository.findAll()
                .stream()
                .filter(office -> office.getId().toString().equals(id))
                .findFirst().map(this::mapToDto).orElse(null);
    }

    public Optional<List<OfficeRetDto>> getOfficesByDistrictNumber(int districtNumber) {
        List<OfficeRetDto> filteredOffices = officeRepository.findAll()
                .stream()
                .filter(office -> office.getDistrict().getId().equals(districtNumber))
                .map(this::mapToDto).toList();

        return filteredOffices.isEmpty() ? Optional.empty() : Optional.of(filteredOffices);
    }

    public void removeOffice(String id){
        Office officeToDelete =
                officeRepository.findAll()
                .stream()
                .filter(office -> office.getId().toString().equals(id))
                .findFirst().
                orElseThrow(() -> new OfficeNotFoundException(id));
        if(officeToDelete.getPhotoId() != null){
            officePhotoService.deletePhotoById(officeToDelete.getPhotoId());
        }
        officeRepository.deleteById(officeToDelete.getId());
   }

   public Office updateOffice(OfficeDto officeDto, MultipartFile file, String id) throws FailedUploadingPhoto {
       UUID ud;
       if (file != null) {
           ud = officePhotoService.addPhoto(file).getId();
       } else {
           ud = null;
       }

       District d = districtRepository.findById(officeDto.district().id()).orElse(null);

       return officeRepository.findAll()
               .stream().filter(office -> office.getId().toString().equals(id))
               .map(existingOffice -> {
                   if(existingOffice.getPhotoId() != null && ud != null){
                       officePhotoService.deletePhotoById(existingOffice.getPhotoId());
                   }
                   if(ud != null){
                       existingOffice.setPhotoId(ud);
                   }
                   existingOffice.setDescription(officeDto.description());
                   existingOffice.setAddress(officeDto.address());
                   if(d != null){
                       existingOffice.setDistrict(d);
                   }
                   existingOffice.setPhoneNumber(officeDto.phoneNumber());
                   existingOffice.setLastUpdatedAt(LocalDateTime.now());
                   return officeRepository.save(existingOffice);
               }).findFirst().orElse(null);
   }


    private OfficeRetDto mapToDto(Office office) {
        PhotoOffice photoOffice = null;
        if(office.getPhotoId() != null){
            photoOffice = officePhotoService.getPhotoById(office.getPhotoId()).orElse(null);
        }

        String content;
        if(photoOffice != null){
            try{
                Resource photo = officePhotoService.asResource(photoOffice);
                byte[] imageBytes = photo.getContentAsByteArray();
                content = Base64.getEncoder().encodeToString(imageBytes);
            }catch(Exception e){
                throw new FileException("Cannot load user picture",e);
            }
        }else{
            content = null;
        }

        return new OfficeRetDto(
                office.getId(),
                new DistrictDto(office.getDistrict().getId(),office.getDistrict().getName()),
                office.getPhoneNumber(),
                office.getAddress(),
                content,
                office.getDescription()
        );
    }
}
