package com.backend.project.controller;

import com.backend.project.dto.FoundItemDto;
import com.backend.project.dto.OfficeDto;
import com.backend.project.model.Office;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.OfficeRepository;
import com.backend.project.security.JWTGenerator;
import com.backend.project.service.OfficeService;
import com.backend.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
                try {
                    byte[] photoBytes = Base64.getDecoder().decode(officeDto.photo());

                    String fileName = username + "_" + System.currentTimeMillis() + ".jpg";
                    Path filePath = Paths.get("src/main/resources/offices", fileName);

                    Files.createDirectories(filePath.getParent());
                    Files.write(filePath, photoBytes);

                    OfficeDto updatedOfficeDto = new OfficeDto(
                            officeDto.district(),
                            officeDto.phoneNumber(),
                            officeDto.address(),
                            filePath.toString(),
                            officeDto.description()
                    );

                    String id = officeService.addOffice(updatedOfficeDto).getId().toString();
                    return ResponseEntity
                            .created(URI.create("/offices/" + id))
                            .build();
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ResponseEntity<>("Error saving photo", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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
    public void deleteOfficeById(@PathVariable String id, HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token != null && jwtGenerator.validateToken(token)) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            UserEntity user = userService.getUserByUsername(username);

        }
        officeService.removeOffice(id);
    }
}
