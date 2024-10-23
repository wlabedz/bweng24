package com.backend.project.controller;

import com.backend.project.dto.OfficeDto;
import com.backend.project.model.Office;
import com.backend.project.repository.OfficeRepository;
import com.backend.project.service.OfficeService;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Console;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class OfficeController {

    private OfficeService officeService;

    public OfficeController(OfficeService bookService) {
        this.officeService = bookService;
    }

    @PostMapping("/offices")
    public ResponseEntity<Office> addOffice(@RequestBody @Valid OfficeDto officeDto) {
        String id = officeService.addOffice(officeDto).getId().toString();
        return ResponseEntity
                .created(URI.create("/offices/" + id))
                .build();
    }


    @GetMapping("/offices")
    public ResponseEntity<Optional<List<Office>>> getOffices(@RequestParam(required = false) Integer districtNumber){
        Optional<List<Office>> offices;

        if (districtNumber != null) {
            offices = officeService.getOfficesByDistrictNumber(districtNumber);
        } else {
            offices = officeService.getAllOffices();
        }

        return ResponseEntity.ok(offices);
    }

    @GetMapping("/offices/{id}")
    public ResponseEntity<Office> getOfficeById(@PathVariable String id) {
        Office office = officeService.getOfficeById(id);
        if (office != null) {
            return ResponseEntity.ok(office);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/offices/{id}")
    public void deleteOfficeById(@PathVariable String id){
        officeService.removeOffice(id);
    }

}
