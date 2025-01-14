package com.backend.project.controllerTests;

import com.backend.project.controller.OfficeController;
import com.backend.project.dto.DistrictDto;
import com.backend.project.dto.OfficeDto;
import com.backend.project.dto.OfficeRetDto;
import com.backend.project.exceptions.OfficeNotFoundException;
import com.backend.project.model.District;
import com.backend.project.model.Office;
import com.backend.project.service.OfficeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OfficeControllerTest {

    private OfficeController officeController;
    private OfficeService officeService;

    @BeforeEach
    public void setUp() {
        officeService = mock(OfficeService.class);
        officeController = new OfficeController(officeService);
    }

    @Test
    public void addOffice_ValidOfficeDto_ReturnsCreatedStatus() {
        DistrictDto districtDto = new DistrictDto(1, "District A");

        OfficeDto officeDto = new OfficeDto(
                districtDto,
                "+43123456789",
                "This is a new office in center of Vienna.",
                "Landstrasse 1010 Vienna",
                "photo"
        );

        Office mockOffice = new Office(
                UUID.randomUUID(),
                new District(districtDto.id(), districtDto.name()),
                officeDto.phoneNumber(),
                officeDto.address(),
                UUID.randomUUID(),
                officeDto.description(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(officeService.addOffice(any(OfficeDto.class))).thenReturn(mockOffice);

        ResponseEntity<String> response = officeController.addOffice(officeDto);

        assertEquals(201, response.getStatusCodeValue());
        verify(officeService, times(1)).addOffice(any(OfficeDto.class));
    }

    @Test
    public void getOffices_OfficesExist_ReturnsListOfOffices() {
        OfficeRetDto officeRetDto = mock(OfficeRetDto.class);
        when(officeService.getAllOffices()).thenReturn(Optional.of(List.of(officeRetDto)));

        ResponseEntity<Optional<List<OfficeRetDto>>> response = officeController.getOffices(null);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().orElseThrow().size());
    }

    @Test
    public void deleteOfficeById_OfficeNotFound_ReturnsNotFoundStatus() {
        UUID random_id = UUID.randomUUID();
        doThrow(new OfficeNotFoundException(random_id.toString()))
                .when(officeService)
                .removeOffice(any(String.class));

        ResponseEntity<String> response = officeController.deleteOfficeById(random_id.toString());

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Office not found with id: " + random_id, response.getBody());
        verify(officeService, times(1)).removeOffice(any(String.class));
    }

    @Test
    public void updateOffice_ValidOfficeDto_ReturnsUpdatedOffice() {
        String officeId = UUID.randomUUID().toString();
        DistrictDto districtDto = new DistrictDto(1, "District A");

        OfficeDto officeDto = new OfficeDto(
                districtDto,
                "+43123456789",
                "This is a new office in center of Vienna.",
                "Landstrasse 1010 Vienna",
                "photo"
        );

        Office updatedOffice = new Office(
                UUID.fromString(officeId),
                new District(districtDto.id(), districtDto.name()),
                officeDto.phoneNumber(),
                officeDto.address(),
                UUID.randomUUID(),
                "This is updated description",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(officeService.updateOffice(any(OfficeDto.class), any(String.class))).thenReturn(updatedOffice);

        ResponseEntity<Office> response = officeController.updateOffice(officeId, officeDto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(officeId, response.getBody().getId().toString());
        assertEquals("This is updated description", response.getBody().getDescription());
        assertEquals("Landstrasse 1010 Vienna", response.getBody().getAddress());
        verify(officeService, times(1)).updateOffice(any(OfficeDto.class), any(String.class));
    }

    @Test
    public void getOfficeById_OfficeExists_ReturnsOfficeDetails() {
        String officeId = UUID.randomUUID().toString();

        OfficeRetDto mockOffice = new OfficeRetDto(
                UUID.fromString(officeId),
                new DistrictDto(1, "District Name"),
                "+43123456789",
                "Office Address, 1234 City",
                "photo-id-12345",
                "Office description"
        );

        when(officeService.getOfficeById(officeId)).thenReturn(mockOffice);

        ResponseEntity<OfficeRetDto> response = officeController.getOfficeById(officeId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(UUID.fromString(officeId), response.getBody().id());
        assertEquals("District Name", response.getBody().district().name());
        assertEquals("+43123456789", response.getBody().phoneNumber());
        assertEquals("Office Address, 1234 City", response.getBody().address());
        assertEquals("photo-id-12345", response.getBody().photo());
        assertEquals("Office description", response.getBody().description());

        verify(officeService, times(1)).getOfficeById(officeId);
    }

    @Test
    public void getOfficeById_OfficeNotFound_ReturnsNotFoundStatus() {
        String officeId = UUID.randomUUID().toString();

        when(officeService.getOfficeById(officeId)).thenReturn(null);

        ResponseEntity<OfficeRetDto> response = officeController.getOfficeById(officeId);

        assertEquals(404, response.getStatusCodeValue());
        verify(officeService, times(1)).getOfficeById(officeId);
    }
}
