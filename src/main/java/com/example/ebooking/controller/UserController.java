package com.example.ebooking.controller;

import com.example.ebooking.dto.user.UserResponseDto;
import com.example.ebooking.dto.user.UserUpdatePasswordDto;
import com.example.ebooking.dto.user.UserUpdateRequestDto;
import com.example.ebooking.dto.user.UserUpdateRoleDto;
import com.example.ebooking.model.User;
import com.example.ebooking.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User management", description = "Endpoints for user")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    public static final String USER_ROLES = "hasAuthority('ROLE_USER') "
            + "or hasAuthority('ROLE_GOLD_USER') "
            + "or hasAuthority('ROLE_PRIVILEGED_USER')";

    private final UserService userService;

    @Operation(summary = "Update user`s role by user id",
            description = "Update user`s role by user id for admin")
    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponseDto updateUserRoleByUserId(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateRoleDto updateRoleDto) {
        return userService.updateRole(id, updateRoleDto);
    }

    @Operation(summary = "Get info by authentication user",
            description = "Get info by authentication user")
    @PreAuthorize(USER_ROLES)
    @GetMapping("/me")
    public UserResponseDto getUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return userService.getInfoByUser(user);
    }

    @Operation(summary = "Update user`s info for authentication user",
            description = "Update user`s info for authentication user")
    @PreAuthorize(USER_ROLES)
    @PutMapping("/me")
    public UserResponseDto updateUser(
            Authentication authentication,
            @RequestBody @Valid UserUpdateRequestDto requestDto
    ) {
        User user = (User) authentication.getPrincipal();
        return userService.updateUser(user, requestDto);
    }

    @Operation(summary = "Update user`s password for authentication user",
            description = "Update user`s password for authentication user")
    @PreAuthorize(USER_ROLES)
    @PutMapping("/me/password")
    public String updateUserPassword(
            Authentication authentication,
            @Valid @RequestBody UserUpdatePasswordDto updatePasswordDto
    ) {
        User user = (User) authentication.getPrincipal();
        return userService.updatePasswordByUser(user, updatePasswordDto);
    }
}
