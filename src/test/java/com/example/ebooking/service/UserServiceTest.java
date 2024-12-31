package com.example.ebooking.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.ebooking.dto.user.UserRegistrationRequestDto;
import com.example.ebooking.dto.user.UserResponseDto;
import com.example.ebooking.dto.user.UserUpdatePasswordDto;
import com.example.ebooking.dto.user.UserUpdateRequestDto;
import com.example.ebooking.dto.user.UserUpdateRoleDto;
import com.example.ebooking.exception.EntityNotFoundException;
import com.example.ebooking.mapper.UserMapper;
import com.example.ebooking.model.Role;
import com.example.ebooking.model.User;
import com.example.ebooking.repository.role.RoleRepository;
import com.example.ebooking.repository.user.UserRepository;
import com.example.ebooking.service.user.UserServiceImpl;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Register user with valid request dto")
    void register_withValidDto_returnResponseDto() {
        Role role = new Role();
        role.setRole(Role.RoleName.ROLE_USER);

        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("john@example.com");
        requestDto.setPassword("john12345");
        requestDto.setRepeatPassword("john12345");
        requestDto.setFirstName("John");
        requestDto.setLastName("User");

        User user = new User();
        user.setId(1L);
        user.setEmail(requestDto.getEmail());
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setRoles(Set.of(role));

        UserResponseDto expected = new UserResponseDto();
        expected.setId(user.getId());
        expected.setFirstName(user.getFirstName());
        expected.setEmail(user.getEmail());
        expected.setLastName(user.getLastName());
        expected.setRoles(Set.of("ROLE_USER"));

        Mockito.when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        Mockito.when(userMapper.toModel(requestDto)).thenReturn(user);
        Mockito.when(roleRepository.findByRole(Role.RoleName.ROLE_USER))
                .thenReturn(Optional.of(role));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.register(requestDto);
        assertEquals(actual, expected);
    }

    @Test
    @DisplayName("Update user`s role with valid id and request dto")
    void updateRole_withValidDtoAndId_returnResponseDto() {
        Role oldRole = new Role();
        oldRole.setRole(Role.RoleName.ROLE_USER);

        Set<Role> newRoles = new HashSet<>();
        Role newRole = new Role();
        newRole.setRole(Role.RoleName.ROLE_GOLD_USER);
        newRoles.add(newRole);

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setRoles(Set.of(oldRole));

        UserResponseDto expected = new UserResponseDto();
        expected.setId(user.getId());
        expected.setRoles(Set.of(newRole.toString()));

        UserUpdateRoleDto updateRoleDto = new UserUpdateRoleDto(Set.of("ROLE_GOLD_USER"));

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(roleRepository.findByRoleIn(updateRoleDto.roles()))
                .thenReturn(Optional.of(newRoles));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.updateRole(userId, updateRoleDto);

        assertEquals(expected.getRoles(), actual.getRoles());
    }

    @Test
    @DisplayName("Update user`s role with not exist role")
    void updateRole_withNotValidDto_throwException() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        UserUpdateRoleDto updateRoleDto = new UserUpdateRoleDto(Set.of("ROLE_GOLD_USER"));

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(roleRepository.findByRoleIn(updateRoleDto.roles()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateRole(userId, updateRoleDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Can`t find roles in DB. Roles: "
                        + updateRoleDto.roles());
    }

    @Test
    @DisplayName("Returns info by user with valid user")
    void getInfoByUser_withValidUserId_returnResponseDto() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setFirstName("Bob");

        UserResponseDto expected = new UserResponseDto();
        expected.setId(user.getId());
        expected.setFirstName(user.getFirstName());

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.getInfoByUser(user);

        assertEquals(expected.getFirstName(), actual.getFirstName());
    }

    @Test
    @DisplayName("Update user with valid user and request dto")
    void updateUser_withValidUserAndDto_returnResponseDto() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setFirstName("Bob");

        UserUpdateRequestDto updateRequestDto = new UserUpdateRequestDto();
        updateRequestDto.setFirstName("Alice");

        UserResponseDto expected = new UserResponseDto();
        expected.setId(user.getId());
        expected.setFirstName(updateRequestDto.getFirstName());

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.updateUser(user, updateRequestDto);

        assertEquals(expected.getFirstName(), actual.getFirstName());
    }

    @Test
    @DisplayName("Update user`s password with valid user and request dto")
    void updatePasswordByUser_withValidUserAndDto_returnMessage() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setFirstName("Bob");

        UserUpdatePasswordDto updateRequestDto = new UserUpdatePasswordDto();
        updateRequestDto.setPassword("user12345");

        String expected = "Your password has been updated";

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);

        String actual = userService.updatePasswordByUser(user, updateRequestDto);

        assertEquals(expected, actual);
    }
}
