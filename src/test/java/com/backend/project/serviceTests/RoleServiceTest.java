package com.backend.project.serviceTests;

import com.backend.project.dto.RoleDto;
import com.backend.project.model.Roles;
import com.backend.project.repository.RoleRepository;
import com.backend.project.service.RolesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RolesService rolesService;

    private RoleDto roleDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        roleDto = new RoleDto("ADMIN");
    }

    @Test
    void addRole_Success_RoleAdded() {
        Roles role = new Roles("ADMIN");
        when(roleRepository.save(any(Roles.class))).thenReturn(role);

        Roles result = rolesService.addRole(roleDto);

        assertNotNull(result);
        assertEquals("ADMIN", result.getName());
        verify(roleRepository, times(1)).save(any(Roles.class));
    }

    @Test
    void addRole_Failure_ThrowsRuntimeException() {
        when(roleRepository.save(any(Roles.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> rolesService.addRole(roleDto));
    }
}

