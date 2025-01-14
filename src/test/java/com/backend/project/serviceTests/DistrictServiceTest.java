package com.backend.project.serviceTests;
import com.backend.project.dto.DistrictDto;
import com.backend.project.model.District;
import com.backend.project.model.Office;
import com.backend.project.repository.DistrictRepository;
import com.backend.project.service.DistrictService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DistrictServiceTest {

    @Mock
    private DistrictRepository districtRepository;

    @InjectMocks
    private DistrictService districtService;

    public DistrictServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllDistricts_WhenDistrictsExist_ReturnsDistrictList() {
        List<District> districts = Arrays.asList(
                new District(1, "District 1", List.of(new Office())),
                new District(2, "District 2", List.of())
        );

        when(districtRepository.findAll()).thenReturn(districts);

        List<District> result = districtService.getAllDistricts();

        assertEquals(2, result.size());
        verify(districtRepository, times(1)).findAll();
    }

    @Test
    void addDistrict_WhenDistrictDtoIsValid_ReturnsAddedDistrict() {
        DistrictDto districtDto = new DistrictDto(1, "District 1");
        District district = new District(1, "District 1", List.of());

        when(districtRepository.save(any(District.class))).thenReturn(district);

        District result = districtService.addDistrict(districtDto);

        assertEquals("District 1", result.getName());
        verify(districtRepository, times(1)).save(any(District.class));
    }

    @Test
    void getDistrictById_WhenDistrictExists_ReturnsDistrict() {
        Office office = new Office();
        District district = new District(1, "District 1", List.of(office));

        when(districtRepository.findAll()).thenReturn(Arrays.asList(district));

        District result = districtService.getDistrictById(1);

        assertEquals("District 1", result.getName());
        assertEquals(1, result.getOffices().size());
        verify(districtRepository, times(1)).findAll();
    }
}
