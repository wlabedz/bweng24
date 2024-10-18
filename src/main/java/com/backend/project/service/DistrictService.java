package com.backend.project.service;

import com.backend.project.dto.DistrictDto;
import com.backend.project.dto.OfficeDto;
import com.backend.project.model.District;
import com.backend.project.model.Office;
import com.backend.project.repository.DistrictRepository;
import com.backend.project.repository.OfficeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistrictService {

    @Autowired
    private DistrictRepository districtRepository;

    public List<District> getAllDistricts() {
        return districtRepository.findAll();
    }

    public District addDistrict(DistrictDto districtDto){
        District district = new District(districtDto.number(), districtDto.name());
        return districtRepository.save(district);
    }

    public District getDistrictById(int id){
        return districtRepository.findAll()
                .stream()
                .filter(district -> district.getId().equals(id))
               .findFirst()
               .orElseThrow();
    }
}
