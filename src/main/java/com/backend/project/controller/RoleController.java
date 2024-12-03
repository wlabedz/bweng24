package com.backend.project.controller;


import com.backend.project.dto.OfficeDto;
import com.backend.project.dto.RoleDto;
import com.backend.project.model.Office;
import com.backend.project.model.Roles;
import com.backend.project.service.RolesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RoleController {

    private final RolesService rolesService;

    @Autowired
    public RoleController(RolesService rolesService){
        this.rolesService = rolesService;
    }


    @PostMapping("/roles")
    public ResponseEntity<Roles> addOffice(@RequestBody @Valid RoleDto roleDto) {
        String id = rolesService.addRole(roleDto).getId().toString();
        return ResponseEntity
                .created(URI.create("/roles/" + id))
                .build();
    }
}
