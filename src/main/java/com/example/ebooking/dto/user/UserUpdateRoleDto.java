package com.example.ebooking.dto.user;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UserUpdateRoleDto(@NotEmpty Set<String> roles) {
}
