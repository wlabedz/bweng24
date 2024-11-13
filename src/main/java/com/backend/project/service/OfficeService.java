package com.backend.project.service;

import com.backend.project.dto.OfficeDto;
import com.backend.project.exceptions.OfficeNotFoundException;
import com.backend.project.model.District;
import com.backend.project.model.Office;
import com.backend.project.repository.DistrictRepository;
import com.backend.project.repository.OfficeRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.server.UID;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OfficeService {

    private OfficeRepository officeRepository;
    private DistrictRepository districtRepository;

    public OfficeService(OfficeRepository officeRepository,
                       DistrictRepository districtRepository) {
        this.officeRepository = officeRepository;
        this.districtRepository = districtRepository;
    }

    public Optional<List<Office>> getAllOffices() {
        List<Office> offices = officeRepository.findAll().stream().peek(office -> office.setPhoto(getFullPhotoPath(office.getPhoto()))).collect(Collectors.toList());
        if(offices.isEmpty()){
            return Optional.empty();
        }else{
            return Optional.of(offices);
        }
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

        Office office = new Office(district, officeDto.phoneNumber(), officeDto.address(),
                officeDto.photo(), officeDto.description());

        return officeRepository.save(office);
    }

    public Office getOfficeById(String id){
        Office of =  officeRepository.findAll()
                .stream()
                .filter(office -> office.getId().toString().equals(id))
               .findFirst().
                orElseThrow(() -> new OfficeNotFoundException(id));

        of.setPhoto(getFullPhotoPath(of.getPhoto()));

        return of;
    }

    public Optional<List<Office>> getOfficesByDistrictNumber(int districtNumber) {
        List<Office> filteredOffices = officeRepository.findAll()
                .stream()
                .filter(office -> office.getDistrict().getId().equals(districtNumber))
                .collect(Collectors.toList());

        return filteredOffices.isEmpty() ? Optional.empty() : Optional.of(filteredOffices);
    }

    public void removeOffice(String id){
        Office officeToDelete =
                officeRepository.findAll()
                .stream()
                .filter(office -> office.getId().toString().equals(id))
                .findFirst().
                orElseThrow(() -> new OfficeNotFoundException(id));

        officeRepository.deleteById(officeToDelete.getId());
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
