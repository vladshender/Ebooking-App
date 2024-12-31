package com.example.ebooking.service.user;

import com.example.ebooking.dto.user.UserRegistrationRequestDto;
import com.example.ebooking.dto.user.UserResponseDto;
import com.example.ebooking.dto.user.UserUpdatePasswordDto;
import com.example.ebooking.dto.user.UserUpdateRequestDto;
import com.example.ebooking.dto.user.UserUpdateRoleDto;
import com.example.ebooking.exception.RegistrationException;
import com.example.ebooking.model.User;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException;

    UserResponseDto updateRole(Long id, UserUpdateRoleDto updateRoleDto);

    UserResponseDto getInfoByUser(User user);

    UserResponseDto updateUser(User user, UserUpdateRequestDto requestDto);

    String updatePasswordByUser(User user, UserUpdatePasswordDto updatePasswordDto);
}
