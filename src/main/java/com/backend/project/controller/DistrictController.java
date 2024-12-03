package com.backend.project.controller;

import com.backend.project.dto.DistrictDto;
import com.backend.project.dto.OfficeDto;
import com.backend.project.model.District;
import com.backend.project.model.Office;
import com.backend.project.service.DistrictService;
import com.backend.project.service.OfficeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DistrictController {

    private final DistrictService districtService;

    @Autowired
    public DistrictController(DistrictService districtService) {
        this.districtService = districtService;
    }

    @PostMapping("/districts")
    public ResponseEntity<District> addDistrict(@RequestBody @Valid DistrictDto districtDto) {
        int id = districtService.addDistrict(districtDto).getId();
        return ResponseEntity
                .created(URI.create("/offices/" + id))
                .build();
    }


    @GetMapping("/districts")
    public List<District> getAllDistricts(){
        return districtService.getAllDistricts();
    }

    @GetMapping("/districts/{id}")
    public ResponseEntity<District> getDistrictById(@PathVariable int id){
        return ResponseEntity
                .ok(districtService.getDistrictById(id));
    }

}
