package com.example.ebooking.dto.user;

import com.example.ebooking.validation.emailvalidator.Email;
import com.example.ebooking.validation.fieldmatch.FieldMatch;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@FieldMatch(first = "password", second = "repeatPassword")
@Data
public class UserRegistrationRequestDto {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Length(min = 8, max = 20)
    private String password;

    @NotBlank
    @Length(min = 8, max = 20)
    private String repeatPassword;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}
