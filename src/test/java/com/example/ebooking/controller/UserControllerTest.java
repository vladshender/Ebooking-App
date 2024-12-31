package com.example.ebooking.controller;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ebooking.dto.user.UserResponseDto;
import com.example.ebooking.dto.user.UserUpdateRequestDto;
import com.example.ebooking.dto.user.UserUpdateRoleDto;
import com.example.ebooking.model.Role;
import com.example.ebooking.util.WithMockCustomUser;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
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

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Update user`s role by user id")
    @Sql(scripts = "classpath:scripts/controller/user/canceled-update-role.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateUserRoleByUserId_withValidRole_returnDto() throws Exception {
        Long userId = 2L;

        UserUpdateRoleDto requestDto = new UserUpdateRoleDto(
                Set.of(Role.RoleName.ROLE_USER.toString()));

        UserResponseDto expected = new UserResponseDto();
        expected.setEmail("bob@example.com");
        expected.setFirstName("Bob");
        expected.setLastName("User");
        expected.setRoles(Set.of(Role.RoleName.ROLE_USER.toString()));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(put("/users/{id}/role", userId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), UserResponseDto.class);
        reflectionEquals(expected, actual, "id");
    }

    @WithMockCustomUser(
            username = "user",
            authorities = {"ROLE_PRIVILEGED_USER"},
            name = "Test User",
            email = "testuser@example.com")
    @Test
    @DisplayName("Returns info by auth user")
    void getUser_returnDto() throws Exception {
        UserResponseDto expected = new UserResponseDto();
        expected.setEmail("bob@example.com");
        expected.setFirstName("Bob");
        expected.setLastName("User");
        expected.setRoles(Set.of(Role.RoleName.ROLE_PRIVILEGED_USER.toString()));

        MvcResult result = mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), UserResponseDto.class);
        reflectionEquals(expected, actual, "id");
    }

    @WithMockCustomUser(
            username = "user",
            authorities = {"ROLE_PRIVILEGED_USER"},
            name = "Test User",
            email = "testuser@example.com")
    @Test
    @DisplayName("Updates auth user with valid request dto")
    @Sql(scripts = "classpath:scripts/controller/user/canceled-update-user.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateUser_withValidRequstDto_returnDto() throws Exception {
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto();
        requestDto.setEmail("bob@example.com");
        requestDto.setFirstName("Bob");
        requestDto.setLastName("Doe");

        UserResponseDto expected = new UserResponseDto();
        expected.setEmail("bob@example.com");
        expected.setFirstName("Bob");
        expected.setLastName("Doe");
        expected.setRoles(Set.of(Role.RoleName.ROLE_PRIVILEGED_USER.toString()));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(put("/users/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), UserResponseDto.class);
        reflectionEquals(expected, actual, "id");
    }
}
