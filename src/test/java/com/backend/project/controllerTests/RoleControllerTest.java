package com.backend.project.controllerTests;

import com.backend.project.controller.RoleController;
import com.backend.project.dto.RoleDto;
import com.backend.project.model.Roles;
import com.backend.project.service.RolesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleControllerTest {

    @Mock
    private RolesService rolesService;

    @InjectMocks
    private RoleController roleController;

    private RoleDto roleDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        roleDto = new RoleDto("ADMIN");
    }

    @Test
    void addRole_Success_ReturnsCreatedRole() {
        Roles role = new Roles("ADMIN");
        when(rolesService.addRole(any(RoleDto.class))).thenReturn(role);

        ResponseEntity<Roles> response = roleController.addOffice(roleDto);

        assertEquals(201, response.getStatusCodeValue());
        assertTrue(response.getHeaders().getLocation().toString().endsWith("/roles/" + role.getId()));
        verify(rolesService, times(1)).addRole(any(RoleDto.class));
    }

    @Test
    void addRole_EmptyName_ThrowsValidationException() {
        RoleDto invalidRole = new RoleDto("");
        assertThrows(Exception.class, () -> {
            roleController.addOffice(invalidRole);
        });
    }
}
