package com.backend.project.service;


import com.backend.project.dto.RoleDto;
import com.backend.project.model.Roles;
import com.backend.project.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RolesService {

    @Autowired
    private RoleRepository roleRepository;

    public Roles addRole(RoleDto roleDto){
        Roles role = new Roles(roleDto.name());

        return roleRepository.save(role);
    }
}
