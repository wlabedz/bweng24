package com.backend.project.service;

import com.backend.project.dto.DistrictDto;
import com.backend.project.dto.OfficeDto;
import com.backend.project.dto.OfficeRetDto;
import com.backend.project.exceptions.OfficeNotFoundException;
import com.backend.project.model.District;
import com.backend.project.model.Office;
import com.backend.project.model.OfficePhoto;
import com.backend.project.repository.DistrictRepository;
import com.backend.project.repository.OfficeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OfficeService {

    private final OfficeRepository officeRepository;
    private final DistrictRepository districtRepository;
    private final OfficePhotoService officePhotoService;

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

    public Office addOffice(OfficeDto officeDto){
        Optional<District> existingDistrict = districtRepository.findById(officeDto.district().id());
        District district;
        if (existingDistrict.isPresent()) {
            district = existingDistrict.get();
        } else {
            district = new District(officeDto.district().id(), officeDto.district().name());
            districtRepository.save(district);
        }

        OfficePhoto officePhoto = officePhotoService.addPhoto(officeDto.photo());

        Office office = new Office(district, officeDto.phoneNumber(), officeDto.address(),
                officePhoto.getId(), officeDto.description());

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

   public Office updateOffice(OfficeDto officeDto, String id) {
       UUID ud;
       if (officeDto.photo() != null) {
           ud = officePhotoService.addPhoto(officeDto.photo()).getId();
       } else {
           ud = null;
       }
       District d = districtRepository.findById(officeDto.district().id()).orElse(null);

       return officeRepository.findAll()
               .stream().filter(office -> office.getId().toString().equals(id))
               .map(existingOffice -> {
                   if(existingOffice.getPhotoId() != null){
                       officePhotoService.deletePhotoById(existingOffice.getPhotoId());
                   }
                   existingOffice.setPhotoId(ud);
                   existingOffice.setDescription(officeDto.description());
                   existingOffice.setAddress(officeDto.address());
                   existingOffice.setDistrict(d);
                   existingOffice.setPhoneNumber(officeDto.phoneNumber());
                   existingOffice.setLastUpdatedAt(LocalDateTime.now());
                   return officeRepository.save(existingOffice);
               }).findFirst().orElse(null);
   }


    private OfficeRetDto mapToDto(Office office) {
        OfficePhoto off = null;
        if(office.getPhotoId() != null){
            off = officePhotoService.getPhotoById(office.getPhotoId()).orElse(null);
        }

        String picture = null;
        if(off != null){
            picture = off.getContent();
        }

        return new OfficeRetDto(
                office.getId(),
                new DistrictDto(office.getDistrict().getId(),office.getDistrict().getName()),
                office.getPhoneNumber(),
                office.getAddress(),
                picture,
                office.getDescription()
        );
    }
}
