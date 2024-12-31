package com.example.ebooking.controller;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ebooking.dto.accommodation.AccommodationRequestDto;
import com.example.ebooking.dto.accommodation.AccommodationResponseDto;
import com.example.ebooking.model.Accommodation;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccommodationControllerTest {
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
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("scripts/controller/accommodation/"
                            + "add-two-accommodation.sql")
            );
        }
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("scripts/controller/accommodation/"
                            + "delete-two-accommodation.sql")
            );
        }
    }

    @Test
    @DisplayName("Returns all accommodations when they exist")
    void getAll_isExistAccommodation_returnListDto() throws Exception {
        AccommodationResponseDto firstAccommodation = new AccommodationResponseDto();
        firstAccommodation.setId(1L);
        firstAccommodation.setType("HOUSE");
        firstAccommodation.setLocation("Kyiv, Ukraine");
        firstAccommodation.setSize("120m");
        firstAccommodation.setAmenities(new HashSet<>(Set.of("Garage", "WiFi")));
        firstAccommodation.setDailyRate(BigDecimal.valueOf(120).setScale(2,
                RoundingMode.HALF_UP));
        firstAccommodation.setAvailability(2);

        AccommodationResponseDto secondAccommodation = new AccommodationResponseDto();
        secondAccommodation.setId(2L);
        secondAccommodation.setType("CONDO");
        secondAccommodation.setLocation("Lviv, Ukraine");
        secondAccommodation.setSize("55m");
        secondAccommodation.setAmenities(new HashSet<>(Set.of("Garage", "WiFi")));
        secondAccommodation.setDailyRate(BigDecimal.valueOf(100.00).setScale(2,
                RoundingMode.HALF_UP));
        secondAccommodation.setAvailability(1);

        List<AccommodationResponseDto> expected = List.of(firstAccommodation,
                secondAccommodation);

        MvcResult result = mockMvc.perform(get("/accommodations/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        AccommodationResponseDto[] actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), AccommodationResponseDto[].class);
        assertEquals(2, actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    @DisplayName("Returns accommodation by id")
    void getAccommodationById_withValidId_returnDto() throws Exception {
        Long id = 1L;

        AccommodationResponseDto expected = new AccommodationResponseDto();
        expected.setId(1L);
        expected.setType("HOUSE");
        expected.setLocation("Kyiv, Ukraine");
        expected.setSize("120m");
        expected.setAmenities(new HashSet<>(Set.of("Garage", "WiFi")));
        expected.setDailyRate(BigDecimal.valueOf(120.00).setScale(2,
                RoundingMode.HALF_UP));
        expected.setAvailability(2);

        MvcResult result = mockMvc.perform(get("/accommodations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        AccommodationResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), AccommodationResponseDto.class);

        reflectionEquals(expected, actual, "id");
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Save accommodation with valid request dto")
    @Sql(scripts = "classpath:scripts/controller/accommodation/setting-counter-correctly.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:scripts/controller/accommodation/delete-saved-accommodation.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void save_withValidRequestDto_returnDto() throws Exception {
        AccommodationRequestDto requestDto = new AccommodationRequestDto();
        requestDto.setLocation("Dnipro, Ukraine");
        requestDto.setType("HOUSE");
        requestDto.setDailyRate(BigDecimal.valueOf(150));
        requestDto.setSize("60m");
        requestDto.setAmenities(Set.of("WiFi"));
        requestDto.setAvailability(2);

        AccommodationResponseDto expected = new AccommodationResponseDto();
        expected.setAvailability(requestDto.getAvailability());
        expected.setAmenities(requestDto.getAmenities());
        expected.setDailyRate(requestDto.getDailyRate());
        expected.setSize(requestDto.getSize());
        expected.setLocation(requestDto.getLocation());
        expected.setType(requestDto.getType());

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/accommodations")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        AccommodationResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), AccommodationResponseDto.class);

        reflectionEquals(expected, actual, "id");
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Update accommodation with valid request dto")
    @Sql(scripts = "classpath:scripts/controller/accommodation/canceled-updated-accommodation.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void update_withValidRequestDtoAndId_returnDto() throws Exception {
        AccommodationRequestDto requestDto = new AccommodationRequestDto();
        requestDto.setType("HOUSE");
        requestDto.setLocation("Kyiv, Ukraine");
        requestDto.setSize("120m");
        requestDto.setAmenities(Set.of(Accommodation.Amenities.WiFi.toString()));
        requestDto.setDailyRate(BigDecimal.valueOf(120));
        requestDto.setAvailability(1);

        AccommodationResponseDto expected = new AccommodationResponseDto();
        expected.setType("HOUSE");
        expected.setLocation("Kyiv, Ukraine");
        expected.setSize("120m");
        expected.setAmenities(Set.of("WiFі"));
        expected.setDailyRate(BigDecimal.valueOf(120));
        expected.setAvailability(1);

        Long accommodationId = 1L;

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(put("/accommodations/{id}", accommodationId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        AccommodationResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), AccommodationResponseDto.class);

        reflectionEquals(expected, actual, "id");
    }
}
