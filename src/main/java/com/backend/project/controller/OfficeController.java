package com.backend.project.controller;

import com.backend.project.dto.OfficeDto;
import com.backend.project.dto.OfficeRetDto;
import com.backend.project.model.Office;
import com.backend.project.model.UserEntity;
import com.backend.project.security.JWTGenerator;
import com.backend.project.service.OfficeService;
import com.backend.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class OfficeController {

    private OfficeService officeService;
    private final JWTGenerator jwtGenerator;
    private final UserService userService;

    public OfficeController(OfficeService bookService, JWTGenerator jwtGenerator, UserService userService) {

        this.officeService = bookService;
        this.jwtGenerator = jwtGenerator;
        this.userService = userService;
    }

    @PostMapping("/offices")
    public ResponseEntity addOffice(@RequestBody @Valid OfficeDto officeDto, HttpServletRequest request) {

        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token != null && jwtGenerator.validateToken(token)) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            UserEntity user = userService.getUserByUsername(username);

            if (user != null) {
                String id = officeService.addOffice(officeDto).getId().toString();
                return ResponseEntity
                        .created(URI.create("/offices/" + id))
                        .build();
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }


    @PutMapping("/offices/{id}")
    public ResponseEntity updateUser(@PathVariable String id, @RequestBody @Valid OfficeDto officeDto, HttpServletRequest request){
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
    public void deleteOfficeById(@PathVariable String id) {
        officeService.removeOffice(id);
    }


}
