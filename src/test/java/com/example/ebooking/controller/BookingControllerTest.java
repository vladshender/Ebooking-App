package com.example.ebooking.controller;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ebooking.dto.booking.BookingRequestDto;
import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.dto.booking.UpdateBookingStatusRequestDto;
import com.example.ebooking.model.Booking;
import com.example.ebooking.util.WithMockCustomUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
public class BookingControllerTest {
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
                    new ClassPathResource("scripts/controller/booking/add-accommodation.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("scripts/controller/booking/add-three-booking.sql")
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
                    new ClassPathResource("scripts/controller/booking/delete-bookings.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("scripts/controller/booking/delete-accommodation.sql")
            );
        }
    }

    @WithMockCustomUser(
            username = "user",
            authorities = {"ROLE_PRIVILEGED_USER"},
            name = "Test User",
            email = "testuser@example.com")
    @Test
    @DisplayName("Save booking with valid request dto")
    @Sql(scripts = "classpath:scripts/controller/booking/setting-counter-correctly.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:scripts/controller/booking/delete-saved-booking.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void save_validRequestDto_returnResponseDto() throws Exception {
        Long accommodationId = 1L;

        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setAccommodationId(accommodationId);
        requestDto.setCheckInDate(LocalDateTime.of(2025, 1, 13, 14, 0));
        requestDto.setCheckOutDate(LocalDateTime.of(2025, 1, 15, 14, 0));

        BookingResponseDto expected = new BookingResponseDto();
        expected.setId(1L);
        expected.setCheckInDate(LocalDateTime.of(2025, 1, 13, 14, 0));
        expected.setCheckOutDate(LocalDateTime.of(2025, 1, 15, 14, 0));
        expected.setAccommodationId(accommodationId);

        String userName = "Bob User";
        String status = "PENDING";
        expected.setUserName(userName);
        expected.setStatus(status);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/bookings")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), BookingResponseDto.class);
        reflectionEquals(expected, actual, "id");
    }

    @WithMockCustomUser(
            username = "user",
            authorities = {"ROLE_PRIVILEGED_USER"},
            name = "Test User",
            email = "testuser@example.com")
    @Test
    @DisplayName("Returns all bookings by user")
    void getAllBookingByAuthUser_isExistBookings_returnListDto() throws Exception {
        Long accommodationId = 1L;
        String userName = "Bob User";
        String status = "PENDING";

        BookingResponseDto firstBooking = new BookingResponseDto();
        firstBooking.setId(1L);
        firstBooking.setCheckInDate(LocalDateTime.of(2025, 1, 27, 14, 0));
        firstBooking.setCheckOutDate(LocalDateTime.of(2025, 1, 28, 11, 0));
        firstBooking.setAccommodationId(accommodationId);
        firstBooking.setUserName(userName);
        firstBooking.setStatus(status);

        BookingResponseDto secondBooking = new BookingResponseDto();
        secondBooking.setId(2L);
        secondBooking.setCheckInDate(LocalDateTime.of(2024, 2, 21, 12, 0));
        secondBooking.setCheckOutDate(LocalDateTime.of(2024, 2, 23, 14, 0));
        secondBooking.setAccommodationId(accommodationId);
        secondBooking.setUserName(userName);
        secondBooking.setStatus(status);

        BookingResponseDto thirdBooking = new BookingResponseDto();
        thirdBooking.setId(3L);
        thirdBooking.setCheckInDate(LocalDateTime.of(2024, 1, 30, 14, 0));
        thirdBooking.setCheckOutDate(LocalDateTime.of(2024, 1, 31, 11, 0));
        thirdBooking.setAccommodationId(accommodationId);
        thirdBooking.setUserName(userName);
        thirdBooking.setStatus(status);

        List<BookingResponseDto> expected = List.of(firstBooking, secondBooking,
                thirdBooking);

        MvcResult result = mockMvc.perform(get("/bookings/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponseDto[] actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), BookingResponseDto[].class);
        assertEquals(3, actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @WithMockCustomUser(
            username = "user",
            authorities = {"ROLE_PRIVILEGED_USER"},
            name = "Test User",
            email = "testuser@example.com")
    @Test
    @DisplayName("Returns booking by booking id for user")
    void getBookingByIdForAuthUser_withValidId_returnResponseDto()
            throws Exception {
        Long bookingId = 1L;
        Long accommodationId = 1L;
        String userName = "Bob User";
        String status = "PENDING";

        BookingResponseDto expected = new BookingResponseDto();
        expected.setId(1L);
        expected.setCheckInDate(LocalDateTime.of(2025, 1, 27, 14, 0));
        expected.setCheckOutDate(LocalDateTime.of(2025, 1, 28, 11, 0));
        expected.setAccommodationId(accommodationId);
        expected.setUserName(userName);
        expected.setStatus(status);

        MvcResult result = mockMvc.perform(get("/bookings/{id}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), BookingResponseDto.class);
        reflectionEquals(expected, actual);
    }

    @WithMockCustomUser(
            username = "user",
            authorities = {"ROLE_PRIVILEGED_USER"},
            name = "Test User",
            email = "testuser@example.com")
    @Test
    @DisplayName("Update booking by booking id with valid request dto")
    @Sql(scripts = "classpath:scripts/controller/booking/canceled-update-booking.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateBookingByIdForAuthUser_withValidRequestDtoAndId_returnDto() throws Exception {
        Long accommodationId = 1L;

        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setAccommodationId(accommodationId);
        requestDto.setCheckInDate(LocalDateTime.of(2025, 1, 30, 14, 0));
        requestDto.setCheckOutDate(LocalDateTime.of(2025, 2, 1, 14, 0));

        Long bookingId = 3L;

        BookingResponseDto expected = new BookingResponseDto();
        expected.setId(bookingId);
        expected.setCheckInDate(requestDto.getCheckInDate());
        expected.setCheckOutDate(requestDto.getCheckOutDate());
        expected.setAccommodationId(accommodationId);

        String userName = "Bob User";
        String status = "PENDING";
        expected.setUserName(userName);
        expected.setStatus(status);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(put("/bookings/{id}",
                        expected.getId())
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), BookingResponseDto.class);
        reflectionEquals(expected, actual, "id");
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Update booking`s status by booking id")
    @Sql(scripts = "classpath:scripts/controller/booking/canceled-updated-status.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateStatusById_withValidRequestDtoAndId_returnDto() throws Exception {
        UpdateBookingStatusRequestDto requestDto = new UpdateBookingStatusRequestDto();
        requestDto.setStatus(Booking.Status.CONFIRMED.toString());

        Long bookingId = 2L;

        BookingResponseDto expected = new BookingResponseDto();
        expected.setId(bookingId);
        expected.setCheckInDate(LocalDateTime.of(2024, 2, 21, 12, 0));
        expected.setCheckOutDate(LocalDateTime.of(2024, 2, 23, 14, 0));
        expected.setAccommodationId(1L);

        String userName = "Bob User";
        String status = "CONFIRMED";
        expected.setUserName(userName);
        expected.setStatus(status);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(put("/bookings/status/{id}", expected.getId())
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), BookingResponseDto.class);
        assertEquals(expected.getStatus(), actual.getStatus());
    }
}
