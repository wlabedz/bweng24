package com.backend.project.serviceTests;

import com.backend.project.dto.DistrictDto;
import com.backend.project.dto.OfficeDto;
import com.backend.project.dto.OfficeRetDto;
import com.backend.project.exceptions.OfficeNotFoundException;
import com.backend.project.model.District;
import com.backend.project.model.Office;
import com.backend.project.model.OfficePhoto;
import com.backend.project.repository.DistrictRepository;
import com.backend.project.repository.OfficeRepository;
import com.backend.project.service.OfficePhotoService;
import com.backend.project.service.OfficeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OfficeServiceTest {

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private DistrictRepository districtRepository;

    @Mock
    private OfficePhotoService officePhotoService;

    @InjectMocks
    private OfficeService officeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void addOffice_Success_OfficeAdded() {
        DistrictDto districtDto = new DistrictDto(1, "District A");
        OfficeDto officeDto = new OfficeDto(
                districtDto,
                "+43123456789",
                "This is a new office in center of Vienna.",
                "Landstrasse 1010 Vienna",
                "photo"
        );

        when(districtRepository.findById(anyInt())).thenReturn(Optional.of(new District()));

        OfficePhoto mockOfficePhoto = mock(OfficePhoto.class);
        when(officePhotoService.addPhoto(anyString())).thenReturn(mockOfficePhoto);

        Office mockOffice = new Office(
                new District(districtDto.id(), districtDto.name()),
                officeDto.phoneNumber(),
                officeDto.address(),
                UUID.randomUUID(),
                officeDto.description()
        );
        when(officeRepository.save(any(Office.class))).thenReturn(mockOffice);

        Office office = officeService.addOffice(officeDto);

        assertNotNull(office);
        verify(officeRepository, times(1)).save(any(Office.class));
        verify(officePhotoService, times(1)).addPhoto(anyString());
    }


    @Test
    public void getAllOffices_Success_OfficesRetrieved() {
        District district = new District(1, "District A");
        Office office1 = new Office(
                district,
                "+43123456789",
                "Landstrasse 1010 Vienna",
                UUID.randomUUID(),
                "This is short description"
        );
        Office office2 = new Office(
                district,
                "+43198765432",
                "Vorgartenstrasse 1010 Vienna",
                UUID.randomUUID(),
                "This is short description"
        );

        List<Office> offices = List.of(office1, office2);

        when(officeRepository.findAll()).thenReturn(offices);

        Optional<List<OfficeRetDto>> result = officeService.getAllOffices();

        assertTrue(result.isPresent(), "The result should be present.");
        assertEquals(2, result.get().size(), "The result size should match the number of offices.");
        assertEquals(office1.getAddress(), result.get().get(0).address(), "The first office's address should match.");
        assertEquals(office2.getDescription(), result.get().get(1).description(), "The second office's description should match.");
    }

    @Test
    public void getOfficesByDistrictNumber_Success_OfficesRetrievedByDistrict() {
        District district1 = new District(1, "District A");
        District district2 = new District(2, "District B");

        Office office1 = new Office(district1, "+43123456789", "Landstrasse 12", UUID.randomUUID(), "Description 1");
        Office office2 = new Office(district1, "+43198765432", "Vorgartenstrasse 5", UUID.randomUUID(), "Description 2");
        Office office3 = new Office(district2, "+43111222333", "Alfredgasse 22", UUID.randomUUID(), "Description 3");

        when(officeRepository.findAll()).thenReturn(List.of(office1, office2, office3));

        Optional<List<OfficeRetDto>> result = officeService.getOfficesByDistrictNumber(1);

        assertTrue(result.isPresent(), "The result should be present.");
        assertEquals(2, result.get().size(), "The result size should match the number of offices in the district.");
        assertEquals("Landstrasse 12", result.get().get(0).address(), "The first office's address should match.");
        assertEquals("Vorgartenstrasse 5", result.get().get(1).address(), "The second office's address should match.");
    }

    @Test
    public void removeOffice_Success_OfficeRemoved() {
        UUID id = UUID.randomUUID();
        Office office = new Office();
        office.setId(id);
        when(officeRepository.findAll()).thenReturn(List.of(office));

        officeService.removeOffice(id.toString());

        verify(officeRepository, times(1)).deleteById(id);
    }

    @Test
    public void mapToDto_UsingReflection_OfficeMappedToDto() throws Exception {
        UUID officeId = UUID.randomUUID();
        UUID photoId = UUID.randomUUID();
        District district = new District(1, "District A");
        Office office = new Office(district, "+43123456789", "Landstrasse 12", photoId, "Short description");
        OfficePhoto officePhoto = new OfficePhoto("Picture of our office");

        when(officePhotoService.getPhotoById(photoId)).thenReturn(Optional.of(officePhoto));

        Method mapToDtoMethod = OfficeService.class.getDeclaredMethod("mapToDto", Office.class);
        mapToDtoMethod.setAccessible(true);

        OfficeRetDto result = (OfficeRetDto) mapToDtoMethod.invoke(officeService, office);

        assertNotNull(result, "The result should not be null.");
        assertEquals("District A", result.district().name(), "The district name should match.");
        assertEquals("+43123456789", result.phoneNumber(), "The phone number should match.");
        assertEquals("Landstrasse 12", result.address(), "The address should match.");
        assertEquals("Short description", result.description(), "The description should match.");
    }



}