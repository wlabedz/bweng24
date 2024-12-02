package com.backend.project.service;


import com.backend.project.dto.RoleDto;
import com.backend.project.model.Roles;
import com.backend.project.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RolesService {

    private final RoleRepository roleRepository;

    public RolesService(RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    public Roles addRole(RoleDto roleDto){
        Roles role = new Roles(roleDto.name());

        return roleRepository.save(role);
    }
}
