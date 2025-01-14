package com.backend.project.controllerTests;

import com.backend.project.controller.DistrictController;
import com.backend.project.dto.DistrictDto;
import com.backend.project.model.District;
import com.backend.project.model.Office;
import com.backend.project.service.DistrictService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DistrictControllerTest {

    @Mock
    private DistrictService districtService;

    @InjectMocks
    private DistrictController districtController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addDistrict_WithValidData_CreatesDistrict() {
        DistrictDto districtDto = new DistrictDto(1, "District 1");
        District district = new District(1, "District 1", List.of());

        when(districtService.addDistrict(districtDto)).thenReturn(district);

        ResponseEntity<District> response = districtController.addDistrict(districtDto);

        assertEquals(201, response.getStatusCodeValue());
        verify(districtService, times(1)).addDistrict(districtDto);
    }

    @Test
    void getAllDistricts_WhenCalled_ReturnsListOfDistricts() {
        List<District> districts = Arrays.asList(
                new District(1, "District 1", List.of(new Office())),
                new District(2, "District 2", List.of())
        );

        when(districtService.getAllDistricts()).thenReturn(districts);

        List<District> response = districtController.getAllDistricts();

        assertEquals(2, response.size());
        assertEquals("District 1", response.get(0).getName());
        verify(districtService, times(1)).getAllDistricts();
    }

    @Test
    void getDistrictById_WithValidId_ReturnsDistrict() {
        District district = new District(1, "District 1", List.of(new Office()));

        when(districtService.getDistrictById(1)).thenReturn(district);

        ResponseEntity<District> response = districtController.getDistrictById(1);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("District 1", response.getBody().getName());
        verify(districtService, times(1)).getDistrictById(1);
    }
}
