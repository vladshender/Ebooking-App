package com.example.ebooking.dto.user;

import com.example.ebooking.validation.emailvalidator.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginRequestDto {
    @NotBlank
    @Email
    @Size(min = 8, max = 20)
    private String email;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;
}
