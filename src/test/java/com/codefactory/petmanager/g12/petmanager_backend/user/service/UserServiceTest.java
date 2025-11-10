package com.codefactory.petmanager.g12.petmanager_backend.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.codefactory.petmanager.g12.petmanager_backend.auth.controller.dto.RoleDTO;
import com.codefactory.petmanager.g12.petmanager_backend.auth.model.Role;
import com.codefactory.petmanager.g12.petmanager_backend.auth.repository.RoleRepository;
import com.codefactory.petmanager.g12.petmanager_backend.user.controller.dto.UserRequestDTO;
import com.codefactory.petmanager.g12.petmanager_backend.user.controller.dto.UserResponseDTO;
import com.codefactory.petmanager.g12.petmanager_backend.user.mapper.UserMapper;
import com.codefactory.petmanager.g12.petmanager_backend.user.model.User;
import com.codefactory.petmanager.g12.petmanager_backend.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponseDTO testUserResponse;
    private UserRequestDTO testUserRequest;
    private Role testRole;
    private RoleDTO testRoleDTO;

    @BeforeEach
    void setUp() {
        // Configurar Role y RoleDTO
        testRole = new Role();
        testRole.setId(1);
        testRole.setName("EMPLOYEE");
        testRole.setDescription("Employee Role");

        testRoleDTO = new RoleDTO();
        testRoleDTO.setId(1);
        testRoleDTO.setName("EMPLOYEE");
        testRoleDTO.setDescription("Employee Role");

        // Configurar User
        testUser = new User();
        testUser.setId(1);
        testUser.setIdNumber("12345678");
        testUser.setIdType("CC");
        testUser.setName("Test User");
        testUser.setEmail("test@test.com");
        testUser.setPassword("hashedPassword");
        testUser.setPhoneNumber("1234567890");
        testUser.setRole(testRole);
        testUser.setActive(true);

        // Configurar UserResponseDTO
        testUserResponse = new UserResponseDTO();
        testUserResponse.setId(1);
        testUserResponse.setIdNumber("12345678");
        testUserResponse.setIdType("CC");
        testUserResponse.setName("Test User");
        testUserResponse.setEmail("test@test.com");
        testUserResponse.setPhoneNumber("1234567890");
        testUserResponse.setRole(testRoleDTO);
        testUserResponse.setActive(true);

        // Configurar UserRequestDTO
        testUserRequest = new UserRequestDTO();
        testUserRequest.setIdNumber("12345678");
        testUserRequest.setIdType("CC");
        testUserRequest.setName("Test User");
        testUserRequest.setEmail("test@test.com");
        testUserRequest.setPassword("password123");
        testUserRequest.setPhoneNumber("1234567890");
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(userMapper.userToUserResponseDTO(testUser)).thenReturn(testUserResponse);

        UserResponseDTO result = userService.getUserByEmail("test@test.com");

        assertNotNull(result);
        assertEquals(testUserResponse.getEmail(), result.getEmail());
        verify(userRepository).findByEmail("test@test.com");
    }

    @Test
    void getUserByEmail_NotFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            userService.getUserByEmail("test@test.com");
        });
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userMapper.userToUserResponseDTO(testUser)).thenReturn(testUserResponse);

        UserResponseDTO result = userService.getUserById(1);

        assertNotNull(result);
        assertEquals(testUserResponse.getId(), result.getId());
        verify(userRepository).findById(1);
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            userService.getUserById(1);
        });
    }

    @Test
    void getAllUsers_Success() {
        List<User> users = Arrays.asList(testUser);
        List<UserResponseDTO> expectedResponses = Arrays.asList(testUserResponse);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.usersToUserResponseDTOs(users)).thenReturn(expectedResponses);

        List<UserResponseDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponses.get(0).getId(), result.get(0).getId());
        verify(userRepository).findAll();
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByIdNumberAndIdType(anyString(), anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userMapper.userRequestDTOToUser(testUserRequest)).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.userToUserResponseDTO(testUser)).thenReturn(testUserResponse);

        UserResponseDTO result = userService.createUser(testUserRequest);

        assertNotNull(result);
        assertEquals(testUserResponse.getId(), result.getId());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(testUserRequest.getPassword());
    }

    @Test
    void createUser_DuplicateIdNumber() {
        when(userRepository.existsByIdNumberAndIdType(anyString(), anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(testUserRequest);
        });
    }

    @Test
    void createUser_DuplicateEmail() {
        when(userRepository.existsByIdNumberAndIdType(anyString(), anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(testUserRequest);
        });
    }

    @Test
    void changeUserRole_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(1)).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.userToUserResponseDTO(testUser)).thenReturn(testUserResponse);

        UserResponseDTO result = userService.changeUserRole(1, 1);

        assertNotNull(result);
        assertEquals(testUserResponse.getId(), result.getId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changeUserRole_UserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            userService.changeUserRole(1, 1);
        });
    }

    @Test
    void changeUserRole_RoleNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            userService.changeUserRole(1, 1);
        });
    }

    @Test
    void getAllRoles_Success() {
        List<Role> roles = Arrays.asList(testRole);
        List<RoleDTO> expectedResponses = Arrays.asList(testRoleDTO);

        when(roleRepository.findAll()).thenReturn(roles);
        when(userMapper.rolesToRoleDTOs(roles)).thenReturn(expectedResponses);

        List<RoleDTO> result = userService.getAllRoles();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponses.get(0).getId(), result.get(0).getId());
        verify(roleRepository).findAll();
    }

    @Test
    void getRoleById_Success() {
        when(roleRepository.findById(1)).thenReturn(Optional.of(testRole));
        when(userMapper.roleToRoleDTO(testRole)).thenReturn(testRoleDTO);

        RoleDTO result = userService.getRoleById(1);

        assertNotNull(result);
        assertEquals(testRoleDTO.getId(), result.getId());
        verify(roleRepository).findById(1);
    }

    @Test
    void getRoleById_NotFound() {
        when(roleRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            userService.getRoleById(1);
        });
    }
}