package com.example.ebooking.service.user;

import com.example.ebooking.dto.user.UserRegistrationRequestDto;
import com.example.ebooking.dto.user.UserResponseDto;
import com.example.ebooking.dto.user.UserUpdatePasswordDto;
import com.example.ebooking.dto.user.UserUpdateRequestDto;
import com.example.ebooking.dto.user.UserUpdateRoleDto;
import com.example.ebooking.exception.EntityNotFoundException;
import com.example.ebooking.exception.RegistrationException;
import com.example.ebooking.mapper.UserMapper;
import com.example.ebooking.model.Role;
import com.example.ebooking.model.User;
import com.example.ebooking.repository.role.RoleRepository;
import com.example.ebooking.repository.user.UserRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("Email is already registered");
        }

        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByRole(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        user.setRoles(new HashSet<>(Set.of(userRole)));
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserResponseDto updateRole(Long id, UserUpdateRoleDto updateRoleDto) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can`t find user by id: " + id)
        );
        Set<Role> userRoles = roleRepository.findByRoleIn(updateRoleDto.roles()).orElseThrow(
                () -> new EntityNotFoundException("Can`t find roles in DB. Roles: "
                        + updateRoleDto.roles())
        );
        user.setRoles(new HashSet<>(userRoles));
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto getInfoByUser(User user) {
        User userFromDB = userRepository.findById(user.getId()).orElseThrow(
                () -> new EntityNotFoundException("Can`t find user by id: " + user.getId())
        );
        return userMapper.toDto(userFromDB);
    }

    @Transactional
    @Override
    public UserResponseDto updateUser(User user, UserUpdateRequestDto requestDto) {
        User userFromDB = userRepository.findById(user.getId()).orElseThrow(
                () -> new EntityNotFoundException("Can`t find user by id: " + user.getId())
        );
        userMapper.updateUserFromDB(requestDto, userFromDB);
        return userMapper.toDto(userRepository.save(userFromDB));
    }

    @Override
    @Transactional
    public String updatePasswordByUser(User user, UserUpdatePasswordDto updatePasswordDto) {
        User userFromDB = userRepository.findById(user.getId()).orElseThrow(
                () -> new EntityNotFoundException("Can`t find user by id: " + user.getId())
        );
        userFromDB.setPassword(passwordEncoder.encode(updatePasswordDto.getPassword()));
        userRepository.save(userFromDB);
        return "Your password has been updated";
    }
}
