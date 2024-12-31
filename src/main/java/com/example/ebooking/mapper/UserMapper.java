package com.example.ebooking.mapper;

import com.example.ebooking.config.MapperConfig;
import com.example.ebooking.dto.user.UserRegistrationRequestDto;
import com.example.ebooking.dto.user.UserResponseDto;
import com.example.ebooking.dto.user.UserUpdateRequestDto;
import com.example.ebooking.model.Role;
import com.example.ebooking.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    @Mapping(source = "roles", target = "roles")
    UserResponseDto toDto(User user);

    User toModel(UserRegistrationRequestDto requestDto);

    void updateUserFromDB(UserUpdateRequestDto requestDto, @MappingTarget User user);

    default Set<String> setRoles(Set<Role> roles) {
        return roles.stream()
                .map(role -> role.getRole().name().toString())
                .collect(Collectors.toSet());
    }
}
