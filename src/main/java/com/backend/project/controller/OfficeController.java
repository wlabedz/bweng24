package com.backend.project.controller;

import com.backend.project.dto.OfficeDto;
import com.backend.project.dto.OfficeRetDto;
import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.exceptions.OfficeNotFoundException;
import com.backend.project.model.Office;
import com.backend.project.service.OfficeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class OfficeController {

    private final OfficeService officeService;

    @Autowired
    public OfficeController(OfficeService bookService) {
        this.officeService = bookService;
    }

    @PostMapping("/offices")
    public ResponseEntity<String> addOffice(@RequestPart("dto") @Valid OfficeDto officeDto, @RequestPart("file") MultipartFile file) {
       String id;
       try{
           id = officeService.addOffice(officeDto, file).getId().toString();
        }catch(FailedUploadingPhoto ex){
            return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
        return ResponseEntity
                        .created(URI.create("/offices/" + id))
                        .build();
    }


    @PutMapping("/offices/{id}")
    public ResponseEntity<Office> updateOffice(@PathVariable String id, @RequestPart("dto") @Valid OfficeDto officeDto, @RequestPart(value = "file", required = false) MultipartFile file){
        Office office;
        try{
            office = officeService.updateOffice(officeDto, file, id);
        }catch(FailedUploadingPhoto ex){
            return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
        return ResponseEntity.ok(office);
    }


    @GetMapping("/offices")
    public ResponseEntity<Optional<List<OfficeRetDto>>> getOffices(@RequestParam(required = false) Integer districtNumber){
        Optional<List<OfficeRetDto>> offices;

        if (districtNumber != null) {
            offices = officeService.getOfficesByDistrictNumber(districtNumber);
        } else {
            offices = officeService.getAllOffices();
        }

        return ResponseEntity.ok(offices);
    }

    @GetMapping("/offices/{id}")
    public ResponseEntity<OfficeRetDto> getOfficeById(@PathVariable String id) {
        OfficeRetDto office = officeService.getOfficeById(id);
        if (office != null) {
            return ResponseEntity.ok(office);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/offices/{id}")
    public ResponseEntity<String> deleteOfficeById(@PathVariable String id) {
        try{
            officeService.removeOffice(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch(OfficeNotFoundException exception){
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
