package com.example.ebooking.controller;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ebooking.dto.user.UserLoginResponseDto;
import com.example.ebooking.dto.user.UserRegistrationRequestDto;
import com.example.ebooking.dto.user.UserResponseDto;
import com.example.ebooking.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext webApplicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Register new user with valid request dto")
    void register_withValidRequestDto_returnDto() throws Exception {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("dick@example.com");
        requestDto.setPassword("pass12345");
        requestDto.setRepeatPassword("pass12345");
        requestDto.setFirstName("Dick");
        requestDto.setLastName("User");

        UserResponseDto expected = new UserResponseDto();
        expected.setFirstName(requestDto.getFirstName());
        expected.setLastName(requestDto.getLastName());
        expected.setEmail(requestDto.getEmail());
        expected.setRoles(Set.of(Role.RoleName.ROLE_USER.toString()));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/auth/registration")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), UserResponseDto.class);
        reflectionEquals(expected, actual, "id");
    }

    @Test
    @DisplayName("Login user with valid request dto")
    void login_withValidRequestDto_returnToken() throws Exception {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("bob@example.com");
        requestDto.setPassword("user12345");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserLoginResponseDto dto = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), UserLoginResponseDto.class);

        assertTrue(StringUtils.hasText(dto.token()));
    }
}
