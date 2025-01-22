package com.backend.project.serviceTests;

import com.backend.project.dto.*;
import com.backend.project.exceptions.*;
import com.backend.project.model.*;
import com.backend.project.repository.RoleRepository;
import com.backend.project.repository.UserPhotoRepository;
import com.backend.project.repository.UserRepository;
import com.backend.project.security.JWTGenerator;
import com.backend.project.service.UserPhotoService;
import com.backend.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTGenerator jwtGenerator;

    @Mock
    private UserPhotoService userPhotoService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserByUsername_Success_UserFound() {
        String username = "myUsername";
        UserEntity mockUser = new UserEntity();
        mockUser.setUsername(username);

        when(userRepository.findAll()).thenReturn(List.of(mockUser));

        UserEntity user = userService.getUserByUsername(username);

        assertNotNull(user);
        assertEquals(username, user.getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserByUsername_UserNotFound_ThrowsUserNotFoundException() {
        String username = "nonexistent";

        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByUsername(username));
    }

    @Test
    void getUserByRequest_Success_UserDtoReturned() throws Exception {
        String username = "myUser";
        String externalId = "externalId";
        UserEntity mockUser = new UserEntity();
        mockUser.setUsername(username);
        PhotoUser mockPhoto = new PhotoUser(externalId);
        mockPhoto.setName("Photo");
        mockUser.setPhoto(mockPhoto.getId());

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer validToken");
        when(jwtGenerator.getUsernameFromJWT("validToken")).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(userPhotoService.getPhotoById(mockPhoto.getId())).thenReturn(Optional.of(mockPhoto));

        Resource r1 = new ByteArrayResource(mockPhoto.getName().getBytes());
        when(userPhotoService.asResource(mockPhoto)).thenReturn(r1);

        UserDto userDto = userService.getUserByRequest(mockRequest);

        String r1encoded = Base64.getEncoder().encodeToString(r1.getContentAsByteArray());

        assertNotNull(userDto);
        assertEquals(username, userDto.username());
        assertEquals(r1encoded,userDto.photo());
        verify(jwtGenerator, times(1)).getUsernameFromJWT("validToken");
    }

    @Test
    void removeByUsername_Success_UserRemoved() {
        String username = "myUsername";
        UserEntity mockUser = new UserEntity();
        mockUser.setUsername(username);

        PhotoUser mockPhoto = new PhotoUser("externalId");
        mockUser.setPhoto(mockPhoto.getId());
        mockUser.setId(UUID.randomUUID());

        when(userRepository.findAll()).thenReturn(List.of(mockUser));

        userService.removeByUsername(username);

        verify(userRepository, times(1)).deleteById(mockUser.getId());
        verify(userPhotoService, times(1)).deletePhotoById(mockUser.getPhoto());
    }

    @Test
    void registerUser_Success_UserRegistered() throws UsernameTakenException, EmailTakenException {
        RegisterDto registerDto = new RegisterDto(
                "John",
                "Smith",
                "jsmith",
                "test@example.com",
                "Password123!",
                "Mr",
                "US"
        );
        UserEntity mockUser = new UserEntity(registerDto.getName(), registerDto.getSurname(),
                registerDto.getMail(), registerDto.getUsername(), "encodedPassword",
                registerDto.getSalutation(), registerDto.getCountry());
        Roles role = new Roles("USER");

        when(userRepository.existsByUsername(registerDto.getUsername())).thenReturn(false);
        when(userRepository.existsByMail(registerDto.getMail())).thenReturn(false);
        when(passwordEncoder.encode(registerDto.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(UserEntity.class))).thenReturn(mockUser);

        UserEntity registeredUser = userService.registerUser(registerDto);

        assertNotNull(registeredUser);
        assertEquals(registerDto.getUsername(), registeredUser.getUsername());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void login_Success_TokenGenerated() {
        LoginDto loginDto = new LoginDto("jsmith", "Password123!");
        Authentication mockAuthentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(jwtGenerator.generateToken(mockAuthentication)).thenReturn("jwtToken");

        AuthResponseDto response = userService.login(loginDto);

        assertNotNull(response);
        assertEquals("jwtToken", response.getAccessToken());
    }

    @Test
    void updateUser_WhenAdmin_UpdatesUserSuccessfully() throws Exception {
        String token = "Bearer someToken";
        String username = "oldUsername";
        UserPatchDto userPatchDto = new UserPatchDto("newUsername", "John", "Doe", "john.doe@example.com", "Mr.", "US");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);

        UserEntity existingUser = new UserEntity();
        existingUser.setUsername(username);
        existingUser.setMail("old@example.com");

        when(jwtGenerator.getUsernameFromJWT("someToken")).thenReturn("admin");
        when(jwtGenerator.getRolesFromJWT("someToken")).thenReturn(List.of("ADMIN"));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("newUsername")).thenReturn(Optional.empty());
        when(userRepository.findByMail("john.doe@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(username, userPatchDto, request);

        assertEquals("newUsername", result.username());
        assertEquals("John", result.name());
        assertEquals("Doe", result.surname());
        assertEquals("john.doe@example.com", result.mail());
        assertEquals("US", result.country());
        assertEquals("Mr.", result.salutation());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_WhenUserUpdatesSelf_UpdatesUserSuccessfully() throws Exception {
        String token = "Bearer someToken";
        String username = "user1";
        UserPatchDto userPatchDto = new UserPatchDto(null, "John", null, null, null, null);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);

        UserEntity existingUser = new UserEntity();
        existingUser.setUsername(username);

        when(jwtGenerator.getUsernameFromJWT("someToken")).thenReturn(username);
        when(jwtGenerator.getRolesFromJWT("someToken")).thenReturn(List.of("USER"));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(username, userPatchDto, request);

        assertEquals("John", result.name());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_WhenInvalidToken_ThrowsInvalidTokenException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        UserPatchDto userPatchDto = new UserPatchDto(null, null, null, null, null, null);

        assertThrows(InvalidToken.class, () -> userService.updateUser("user1", userPatchDto, request));
    }

    @Test
    void updateUser_WhenUserNotAllowed_ThrowsNotAllowedException() {
        String token = "Bearer someToken";
        String username = "user1";
        UserPatchDto userPatchDto = new UserPatchDto(null, null, null, null, null, null);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);

        when(jwtGenerator.getUsernameFromJWT("someToken")).thenReturn("user2");
        when(jwtGenerator.getRolesFromJWT("someToken")).thenReturn(List.of("USER"));

        assertThrows(NotAllowedException.class, () -> userService.updateUser(username, userPatchDto, request));
    }

    @Test
    void updateUser_WhenNewUsernameForbidden_ThrowsUsernameForbiddenException() {
        String token = "Bearer someToken";
        String username = "user1";
        UserPatchDto userPatchDto = new UserPatchDto("adminUser", null, null, null, null, null);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);

        when(jwtGenerator.getUsernameFromJWT("someToken")).thenReturn("user1");
        when(jwtGenerator.getRolesFromJWT("someToken")).thenReturn(List.of("USER"));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new UserEntity()));

        assertThrows(UsernameForbiddenException.class, () -> userService.updateUser(username, userPatchDto, request));
    }

    @Test
    void updateUser_WhenEmailTaken_ThrowsEmailTakenException() {
        String token = "Bearer someToken";
        String username = "user1";
        UserPatchDto userPatchDto = new UserPatchDto(null, null, null, "taken@example.com", null, null);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);

        UserEntity existingUser = new UserEntity();
        existingUser.setUsername(username);
        existingUser.setMail("user1@example.com");

        when(jwtGenerator.getUsernameFromJWT("someToken")).thenReturn("user1");
        when(jwtGenerator.getRolesFromJWT("someToken")).thenReturn(List.of("USER"));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByMail("taken@example.com")).thenReturn(Optional.of(new UserEntity()));

        assertThrows(EmailTakenException.class, () -> userService.updateUser(username, userPatchDto, request));
    }


    @Test
    void getAll_WhenUsersWithRoleUserExist_ReturnsListOfUsers() {
        UserEntity user1 = new UserEntity();
        user1.setUsername("user1");
        user1.setRoles(List.of(new Roles("USER")));

        UserEntity user2 = new UserEntity();
        user2.setUsername("user2");
        user2.setRoles(List.of(new Roles("USER")));

        List<UserEntity> users = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.getAll();

        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).username());
        assertEquals("user2", result.get(1).username());
    }

    @Test
    void getAll_WhenNoUsersWithRoleUser_ReturnsEmptyList() {
        UserEntity user1 = new UserEntity();
        user1.setUsername("user1");
        user1.setRoles(List.of(new Roles("ADMIN")));

        UserEntity user2 = new UserEntity();
        user2.setUsername("user2");
        user2.setRoles(List.of(new Roles("ADMIN")));

        List<UserEntity> users = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_WhenNoUsersExist_ReturnsEmptyList() {
        List<UserEntity> users = new ArrayList<>();

        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_WhenUsersWithDifferentRoles_ReturnsOnlyUsersWithRoleUser() {
        UserEntity user1 = new UserEntity();
        user1.setUsername("user1");
        user1.setRoles(List.of(new Roles("USER")));

        UserEntity user2 = new UserEntity();
        user2.setUsername("user2");
        user2.setRoles(List.of(new Roles("ADMIN")));

        UserEntity user3 = new UserEntity();
        user3.setUsername("user3");
        user3.setRoles(List.of(new Roles("USER")));

        List<UserEntity> users = List.of(user1, user2, user3);

        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.getAll();

        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).username());
        assertEquals("user3", result.get(1).username());
    }

    @Test
    void changePassword_WhenValidTokenAndCorrectOldPassword_ChangesPassword() throws InvalidToken, UsernameNotFoundException, InvalidCredentialsException {
        String token = "Bearer validToken";
        String username = "user";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword123!";

        ChangePasswordDto changePasswordDto = new ChangePasswordDto(oldPassword, newPassword, newPassword);

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword("encodedOldPassword");

        when(jwtGenerator.validateToken("validToken")).thenReturn(true);
        when(jwtGenerator.getUsernameFromJWT("validToken")).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        String result = userService.changePassword(changePasswordDto, token);

        assertEquals("encodedNewPassword", result);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changePassword_WhenInvalidToken_ThrowsInvalidTokenException() {
        String token = "Bearer invalidToken";
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("oldPassword", "newPassword123!", "newPassword123!");

        when(jwtGenerator.validateToken("invalidToken")).thenReturn(false);

        assertThrows(InvalidToken.class, () -> userService.changePassword(changePasswordDto, token));
    }

    @Test
    void changePassword_WhenOldPasswordIsIncorrect_ThrowsInvalidCredentialsException() {
        String token = "Bearer validToken";
        String username = "user";
        String oldPassword = "oldPassword";
        ChangePasswordDto changePasswordDto = new ChangePasswordDto(oldPassword, "newPassword", "newPassword");

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword("encodedOldPassword");

        when(jwtGenerator.validateToken("validToken")).thenReturn(true);
        when(jwtGenerator.getUsernameFromJWT("validToken")).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.changePassword(changePasswordDto, token));
    }


    @Test
    void changeEmail_WhenCorrectPassword_ChangesEmail() throws InvalidToken, UsernameNotFoundException, InvalidCredentialsException, EmailTakenException {
        String token = "Bearer validToken";
        String username = "user";
        String oldEmail = "old@gmail.com";
        String newEmail = "new@gmail.com";
        String password = "correctPassword";

        changeEmailDto emailDto = new changeEmailDto(newEmail,password);

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword("encodedPassword");
        user.setMail(oldEmail);

        when(jwtGenerator.validateToken("validToken")).thenReturn(true);
        when(jwtGenerator.getUsernameFromJWT("validToken")).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
        when(userRepository.existsByMail(newEmail)).thenReturn(false);

        String result = userService.changeEmail(emailDto, token);

        assertEquals("Email successfully changed to " + newEmail, result);
        assertEquals(newEmail, user.getMail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changeEmail_WhenPasswordIsIncorrect_ThrowsException() {
        String token = "Bearer validToken";
        String username = "user";
        String password = "incorrectPassword";
        changeEmailDto emailDto = new changeEmailDto("new@gmail.com",password);

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword("encodedPassword");

        when(jwtGenerator.validateToken("validToken")).thenReturn(true);
        when(jwtGenerator.getUsernameFromJWT("validToken")).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.changeEmail(emailDto, token));
    }


    @Test
    void changeEmail_WhenEmailAlreadyExists_ThrowsEmailTakenException() {
        String token = "Bearer validToken";
        String username = "user";
        String newEmail = "123@gmail.com";
        changeEmailDto emailDto = new changeEmailDto(newEmail, "correctPassword");

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword("encodedPassword");

        when(jwtGenerator.validateToken("validToken")).thenReturn(true);
        when(jwtGenerator.getUsernameFromJWT("validToken")).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", user.getPassword())).thenReturn(true);
        when(userRepository.existsByMail(newEmail)).thenReturn(true);

        assertThrows(EmailTakenException.class, () -> userService.changeEmail(emailDto, token));
    }

    @Test
    void mapToDto_WhenNoPicture_ReturnsUserDto() throws Exception {
        UserEntity user = new UserEntity();
        user.setUsername("testUser");
        user.setPhoto(null);
        user.setName("John");
        user.setSurname("Doe");
        user.setMail("john.doe@example.com");
        user.setCountry("USA");
        user.setSalutation("Mr.");

        Method mapToDtoMethod = UserService.class.getDeclaredMethod("mapToDto", UserEntity.class);
        mapToDtoMethod.setAccessible(true);

        UserDto result = (UserDto) mapToDtoMethod.invoke(userService, user);

        assertEquals("testUser", result.username());
        assertEquals("John", result.name());
        assertEquals("Doe", result.surname());
        assertEquals("john.doe@example.com", result.mail());
        assertEquals("USA", result.country());
        assertEquals("Mr.", result.salutation());
        assertNull(result.photo());
    }

    @Test
    void mapToDto_WhenPicture_ReturnsUserDto() throws Exception {
        UserEntity user = new UserEntity();
        user.setUsername("testUser");
        user.setPhoto(UUID.randomUUID());
        user.setName("John");
        user.setSurname("Doe");
        user.setMail("john.doe@example.com");
        user.setCountry("USA");
        user.setSalutation("Mr.");

        PhotoUser mockPhotoUser = new PhotoUser("externalId");
        Resource mockResource = new ByteArrayResource("mockImageContent".getBytes());

        when(userPhotoService.getPhotoById(user.getPhoto())).thenReturn(Optional.of(mockPhotoUser));
        when(userPhotoService.asResource(mockPhotoUser)).thenReturn(mockResource);

        Method mapToDtoMethod = UserService.class.getDeclaredMethod("mapToDto", UserEntity.class);
        mapToDtoMethod.setAccessible(true);

        UserDto result = (UserDto) mapToDtoMethod.invoke(userService, user);

        assertEquals("testUser", result.username());
        assertEquals("John", result.name());
        assertEquals("Doe", result.surname());
        assertEquals("john.doe@example.com", result.mail());
        assertEquals("USA", result.country());
        assertEquals("Mr.", result.salutation());

        String expectedBase64 = Base64.getEncoder().encodeToString("mockImageContent".getBytes());
        assertEquals(expectedBase64, result.photo());
    }
}
