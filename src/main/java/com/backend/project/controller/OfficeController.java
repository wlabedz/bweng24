package com.backend.project.controller;

import com.backend.project.dto.OfficeDto;
import com.backend.project.dto.OfficeRetDto;
import com.backend.project.exceptions.OfficeNotFoundException;
import com.backend.project.model.Office;
import com.backend.project.service.OfficeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class OfficeController {

    private final OfficeService officeService;

    public OfficeController(OfficeService bookService) {

        this.officeService = bookService;
    }

    @PostMapping("/offices")
    public ResponseEntity<String> addOffice(@RequestBody @Valid OfficeDto officeDto) {
        String id = officeService.addOffice(officeDto).getId().toString();
        return ResponseEntity
                        .created(URI.create("/offices/" + id))
                        .build();
    }


    @PutMapping("/offices/{id}")
    public ResponseEntity<Office> updateUser(@PathVariable String id, @RequestBody @Valid OfficeDto officeDto){
        Office office = officeService.updateOffice(officeDto, id);
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
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
