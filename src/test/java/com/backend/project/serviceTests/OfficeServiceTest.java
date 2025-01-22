package com.backend.project.serviceTests;

import com.backend.project.dto.DistrictDto;
import com.backend.project.dto.FoundItemRetDto;
import com.backend.project.dto.OfficeDto;
import com.backend.project.dto.OfficeRetDto;
import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.exceptions.OfficeNotFoundException;
import com.backend.project.model.*;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
    public void addOffice_Success_OfficeAdded() throws FailedUploadingPhoto, InstantiationException, IllegalAccessException {
        DistrictDto districtDto = new DistrictDto(1, "District A");
        OfficeDto officeDto = new OfficeDto(
                districtDto,
                "+43123456789",
                "This is a new office in center of Vienna.",
                "Landstrasse 1010 Vienna"
        );

        when(districtRepository.findById(anyInt())).thenReturn(Optional.of(new District()));

        PhotoOffice mockOfficePhoto = mock(PhotoOffice.class);

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(officePhotoService.addPhoto(multipartFile)).thenReturn(mockOfficePhoto);

        Office mockOffice = new Office(
                new District(districtDto.id(), districtDto.name()),
                officeDto.phoneNumber(),
                officeDto.address(),
                null,
                officeDto.description()
        );
        when(officeRepository.save(any(Office.class))).thenReturn(mockOffice);

        Office office = officeService.addOffice(officeDto,multipartFile);

        assertNotNull(office);
        assertEquals(office.getPhoneNumber(),officeDto.phoneNumber());
        assertEquals(office.getPhotoId(),mockOfficePhoto.getId());
        verify(officeRepository, times(1)).save(any(Office.class));
        verify(officePhotoService, times(1)).addPhoto(any(MultipartFile.class));
    }

    @Test
    public void addOffice_Success_OfficeAndDistrictAdded() throws FailedUploadingPhoto, InstantiationException, IllegalAccessException {
        DistrictDto districtDto = new DistrictDto(1, "District A");
        OfficeDto officeDto = new OfficeDto(
                districtDto,
                "+43123456789",
                "This is a new office in center of Vienna.",
                "Landstrasse 1010 Vienna"
        );

        District district = new District(1,"District A");

        when(districtRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(districtRepository.save(any(District.class))).thenReturn(district);

        PhotoOffice mockOfficePhoto = mock(PhotoOffice.class);

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(officePhotoService.addPhoto(multipartFile)).thenReturn(mockOfficePhoto);

        Office mockOffice = new Office(
                new District(districtDto.id(), districtDto.name()),
                officeDto.phoneNumber(),
                officeDto.address(),
                null,
                officeDto.description()
        );
        when(officeRepository.save(any(Office.class))).thenReturn(mockOffice);

        Office office = officeService.addOffice(officeDto,multipartFile);

        assertNotNull(office);
        assertEquals(officeDto.district().name(), office.getDistrict().getName());
        assertEquals(officeDto.phoneNumber(),office.getPhoneNumber());
        assertEquals(office.getPhotoId(),mockOfficePhoto.getId());
        verify(districtRepository,times(1)).save(any(District.class));
        verify(officeRepository, times(1)).save(any(Office.class));
        verify(officePhotoService, times(1)).addPhoto(any(MultipartFile.class));
    }

    @Test
    void getOfficeById_WhenOfficeExists_ReturnsOfficeDto() {
        District district = new District(1,"District A");
        Office office = new Office(district,"+4310010010","Address1",UUID.randomUUID(),"Description");

        UUID id = UUID.randomUUID();
        office.setId(id);
        List<Office> toReturn = new ArrayList<>();
        toReturn.add(office);
        when(officeRepository.findAll()).thenReturn(toReturn);

        OfficeRetDto result = officeService.getOfficeById(id.toString());

        assertEquals("District A", result.district().name());
        assertEquals("+4310010010", result.phoneNumber());
        verify(officeRepository, times(1)).findAll();
    }

    @Test
    void getOfficeById_WhenOfficeNotFound_ReturnsNull() {
        UUID id = UUID.randomUUID();
        when(officeRepository.findById(id)).thenReturn(Optional.empty());
        assertNull(officeService.getOfficeById(id.toString()));
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
    public void removeOffice_Success_OfficeAndPhotoRemoved() {
        UUID id = UUID.randomUUID();
        UUID photoId = UUID.randomUUID();
        Office office = new Office();
        office.setId(id);
        office.setPhotoId(photoId);
        when(officeRepository.findAll()).thenReturn(List.of(office));

        officeService.removeOffice(id.toString());

        verify(officePhotoService,times(1)).deletePhotoById(photoId);
        verify(officeRepository, times(1)).deleteById(id);
    }

    @Test
    public void mapToDto_UsingReflection_OfficeMappedToDto() throws Exception {
        UUID officeId = UUID.randomUUID();
        UUID photoId = UUID.randomUUID();
        District district = new District(1, "District A");
        Office office = new Office(district, "+43123456789", "Landstrasse 12", photoId, "Short description");
        PhotoOffice officePhoto = new PhotoOffice("ExternalId");
        officePhoto.setId(photoId);
        officePhoto.setName("Photo");

        when(officePhotoService.getPhotoById(photoId)).thenReturn(Optional.of(officePhoto));
        Resource r1 = new ByteArrayResource(officePhoto.getName().getBytes());
        when(officePhotoService.asResource(officePhoto)).thenReturn(r1);

        Method mapToDtoMethod = OfficeService.class.getDeclaredMethod("mapToDto", Office.class);
        mapToDtoMethod.setAccessible(true);

        OfficeRetDto result = (OfficeRetDto) mapToDtoMethod.invoke(officeService, office);

        String r1encoded = Base64.getEncoder().encodeToString(r1.getContentAsByteArray());

        assertNotNull(result, "The result should not be null.");
        assertEquals("District A", result.district().name(), "The district name should match.");
        assertEquals("+43123456789", result.phoneNumber(), "The phone number should match.");
        assertEquals("Landstrasse 12", result.address(), "The address should match.");
        assertEquals("Short description", result.description(), "The description should match.");
        assertEquals(r1encoded,result.photo());
    }


    @Test
    void updateOffice_WhenFileProvided_UpdatesOfficeWithNewPhoto() throws FailedUploadingPhoto {
        String officeId = UUID.randomUUID().toString();
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");

        OfficeDto officeDto = new OfficeDto(
                new DistrictDto(1, "District Name"),
                "123456789",
                "Description",
                "Address"
        );

        UUID newPhotoId = UUID.randomUUID();
        UUID oldPhotoId = UUID.randomUUID();

        Office existingOffice = new Office();
        existingOffice.setId(UUID.fromString(officeId));
        existingOffice.setPhotoId(oldPhotoId);
        existingOffice.setDescription("Old Description");
        existingOffice.setAddress("Old Address");
        existingOffice.setPhoneNumber("987654321");
        existingOffice.setDistrict(new District());

        PhotoOffice toReturn = new PhotoOffice("externalId");
        toReturn.setId(newPhotoId);
        when(officePhotoService.addPhoto(any(MultipartFile.class))).thenReturn(toReturn);
        when(officeRepository.findById(UUID.fromString(officeId))).thenReturn(Optional.of(existingOffice));
        when(officeRepository.findAll()).thenReturn(List.of(existingOffice));
        when(districtRepository.findById(officeDto.district().id())).thenReturn(Optional.of(new District()));
        when(officeRepository.save(any(Office.class))).thenAnswer(invocation -> invocation.getArgument(0)); // it returns the first argument passed to officeRepository.save() method so the Office object, so it acts as if it was saved with changes in the repository

        Office updatedOffice = officeService.updateOffice(officeDto, file, officeId);

        assertNotNull(updatedOffice);
        assertEquals(newPhotoId, updatedOffice.getPhotoId());
        assertEquals(officeDto.description(), updatedOffice.getDescription());
        assertEquals(officeDto.address(), updatedOffice.getAddress());
        assertEquals(officeDto.phoneNumber(), updatedOffice.getPhoneNumber());

        verify(officePhotoService, times(1)).addPhoto(file);
        verify(officePhotoService, times(1)).deletePhotoById(oldPhotoId);
        verify(officeRepository, times(1)).save(existingOffice);
    }

    @Test
    void updateOffice_WhenFileNotProvided_RemovesOldPhoto() throws FailedUploadingPhoto {
        String officeId = UUID.randomUUID().toString();
        MultipartFile file = null;

        OfficeDto officeDto = new OfficeDto(
                new DistrictDto(1, "District Name"),
                "123456789",
                "Description",
                "Address"
        );

        UUID oldPhotoId = UUID.randomUUID();

        Office existingOffice = new Office();
        existingOffice.setId(UUID.fromString(officeId));
        existingOffice.setPhotoId(oldPhotoId);
        existingOffice.setDescription("Old Description");
        existingOffice.setAddress("Old Address");
        existingOffice.setPhoneNumber("987654321");
        existingOffice.setDistrict(new District());

        when(officeRepository.findById(UUID.fromString(officeId))).thenReturn(Optional.of(existingOffice));
        when(officeRepository.findAll()).thenReturn(List.of(existingOffice));
        when(districtRepository.findById(officeDto.district().id())).thenReturn(Optional.of(new District()));
        when(officeRepository.save(any(Office.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Office updatedOffice = officeService.updateOffice(officeDto, file, officeId);

        assertNotNull(updatedOffice);
        assertEquals(officeDto.description(), updatedOffice.getDescription());
        assertEquals(officeDto.address(), updatedOffice.getAddress());
        assertEquals(officeDto.phoneNumber(), updatedOffice.getPhoneNumber());

        verify(officeRepository, times(1)).save(existingOffice);
    }

}
