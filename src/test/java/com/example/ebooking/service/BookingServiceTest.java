package com.example.ebooking.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import com.example.ebooking.dto.booking.BookingFilterParameters;
import com.example.ebooking.dto.booking.BookingRequestDto;
import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.exception.BookingAvailabilityException;
import com.example.ebooking.exception.EntityNotFoundException;
import com.example.ebooking.mapper.BookingMapper;
import com.example.ebooking.model.Accommodation;
import com.example.ebooking.model.Booking;
import com.example.ebooking.model.User;
import com.example.ebooking.repository.accommodation.AccommodationRepository;
import com.example.ebooking.repository.booking.BookingRepository;
import com.example.ebooking.repository.booking.BookingSpecificationBuilder;
import com.example.ebooking.repository.booking.spec.StatusSpecificationProvider;
import com.example.ebooking.repository.user.UserRepository;
import com.example.ebooking.service.booking.BookingServiceImpl;
import com.example.ebooking.service.notification.TelegramNotificationService;
import com.example.ebooking.service.payment.StripePaymentService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingSpecificationBuilder specificationBuilder;
    @Mock
    private TelegramNotificationService notificationService;
    @Mock
    private StripePaymentService paymentService;
    @Mock
    private StatusSpecificationProvider statusSpecificationProvider;

    @Test
    @DisplayName("Save booking with valid input data")
    void save_withValidInputData_returnResponseDto() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Bob");
        user.setLastName("User");

        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setAccommodationId(1L);
        requestDto.setCheckInDate(LocalDateTime.of(2024, 12, 29, 12, 0, 0));
        requestDto.setCheckOutDate(LocalDateTime.of(2024, 12, 30, 14, 0, 0));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setDailyRate(BigDecimal.valueOf(110));

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setAccommodation(accommodation);
        booking.setUser(user);
        booking.setStatus(Booking.Status.PENDING);

        BookingResponseDto expected = new BookingResponseDto();
        expected.setId(booking.getId());
        expected.setAccommodationId(accommodation.getId());
        expected.setStatus(booking.getStatus().toString());
        expected.setUserName(user.getFirstName());
        expected.setCheckInDate(booking.getCheckInDate());
        expected.setCheckOutDate(booking.getCheckOutDate());

        Mockito.when(paymentService.existsByBookingUserIdAndStatus(user.getId()))
                .thenReturn(false);
        Mockito.when(bookingRepository.findByAccommodationId(1L)).thenReturn(List.of());
        Mockito.when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        Mockito.when(bookingMapper.toModel(any(BookingRequestDto.class))).thenReturn(booking);
        Mockito.when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        Mockito.doNothing().when(notificationService)
                .sendBookingCreateMessage(accommodation, user, booking);
        Mockito.when(bookingMapper.toDto(any(Booking.class))).thenReturn(expected);

        BookingResponseDto actual = bookingService.save(user, requestDto);

        assertEquals(expected.getAccommodationId(), actual.getAccommodationId());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getCheckInDate(), actual.getCheckInDate());
        assertEquals(expected.getUserName(), actual.getUserName());
    }

    @Test
    @DisplayName("Save booking when exist overlapping booking`s date")
    void save_withOverlappingDate_throwException() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Bob");
        user.setLastName("User");

        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setAccommodationId(1L);
        requestDto.setCheckInDate(LocalDateTime.of(2024, 12, 29, 12, 0, 0));
        requestDto.setCheckOutDate(LocalDateTime.of(2024, 12, 31, 14, 0, 0));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setDailyRate(BigDecimal.valueOf(110));
        accommodation.setAvailability(1);

        Booking bookingFromDB = new Booking();
        bookingFromDB.setAccommodation(accommodation);
        bookingFromDB.setId(1L);
        bookingFromDB.setCheckInDate(LocalDateTime.of(2024, 12, 28, 12, 0, 0));
        bookingFromDB.setCheckOutDate(LocalDateTime.of(2024, 12, 30, 12, 0, 0));

        Mockito.when(paymentService.existsByBookingUserIdAndStatus(user.getId()))
                .thenReturn(false);
        Mockito.when(bookingRepository.findByAccommodationId(1L))
                .thenReturn(List.of(bookingFromDB));
        Mockito.when(accommodationRepository.findById(1L))
                .thenReturn(Optional.of(accommodation));

        assertThatThrownBy(() -> bookingService.save(user, requestDto))
                .isInstanceOf(BookingAvailabilityException.class)
                .hasMessageContaining(String.format("Accommodation is booked from %s to %s.",
                        bookingFromDB.getCheckInDate(), bookingFromDB.getCheckOutDate()));
    }

    @Test
    @DisplayName("Returns all booking by user with valid user")
    void getAllBookingsByUser_withValidUser_returnListBookings() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Alice");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setAccommodation(new Accommodation());
        booking.getAccommodation().setId(1L);
        booking.setUser(user);
        booking.setStatus(Booking.Status.PENDING);
        booking.setCheckInDate(LocalDateTime.of(2024, 12, 28, 12, 0, 0));
        booking.setCheckOutDate(LocalDateTime.of(2024, 12, 30, 12, 0, 0));

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(booking.getId());
        responseDto.setAccommodationId(booking.getAccommodation().getId());
        responseDto.setStatus(booking.getStatus().toString());
        responseDto.setUserName(user.getFirstName());
        responseDto.setCheckInDate(booking.getCheckInDate());
        responseDto.setCheckOutDate(booking.getCheckOutDate());
        List<BookingResponseDto> expected = List.of(responseDto);

        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> bookings = List.of(booking);
        Page<Booking> bookingPage = new PageImpl<>(bookings, pageable, bookings.size());

        Mockito.when(bookingRepository.findByUserId(user.getId(), pageable))
                .thenReturn(bookingPage);
        Mockito.when(bookingMapper.toListDto(List.of(booking))).thenReturn(expected);

        List<BookingResponseDto> actual = bookingService.getAllBookingsByUser(user, pageable);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Returns all booking by user when bookings not exist")
    void getAllBookingsByUser_withValidUser_throwException() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Alice");

        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> bookings = List.of();
        Page<Booking> bookingPage = new PageImpl<>(bookings, pageable, bookings.size());

        Mockito.when(bookingRepository.findByUserId(user.getId(), pageable))
                .thenReturn(bookingPage);

        assertThatThrownBy(() -> bookingService.getAllBookingsByUser(user, pageable))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Can`t find bookings "
                        + "by user id: " + user.getId());
    }

    @Test
    @DisplayName("Returns booking by id for user with valid user and id")
    void getBookingByIdForUser_withValidUserAndId_returnResponseDto() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Alice");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setAccommodation(new Accommodation());
        booking.getAccommodation().setId(1L);
        booking.setUser(user);
        booking.setStatus(Booking.Status.PENDING);
        booking.setCheckInDate(LocalDateTime.of(2024, 12, 28, 12, 0, 0));
        booking.setCheckOutDate(LocalDateTime.of(2024, 12, 30, 12, 0, 0));

        BookingResponseDto expected = new BookingResponseDto();
        expected.setId(booking.getId());
        expected.setAccommodationId(booking.getAccommodation().getId());
        expected.setStatus(booking.getStatus().toString());
        expected.setUserName(user.getFirstName());
        expected.setCheckInDate(booking.getCheckInDate());
        expected.setCheckOutDate(booking.getCheckOutDate());

        Long bookingId = 1L;

        Mockito.when(bookingRepository.findByUserIdAndId(user.getId(), bookingId))
                .thenReturn(Optional.of(booking));
        Mockito.when(bookingMapper.toDto(any(Booking.class))).thenReturn(expected);

        BookingResponseDto actual = bookingService.getBookingByIdForUser(user, bookingId);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Returns booking by id for user when booking not exist")
    void getBookingByIdForUser_withValidUser_throwException() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Alice");

        Long bookingId = 1L;

        Mockito.when(bookingRepository.findByUserIdAndId(user.getId(), bookingId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingByIdForUser(user, bookingId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Booking with id "
                        + bookingId
                        + " not found for user id: "
                        + user.getId());
    }

    @Test
    @DisplayName("Update booking with valid input data")
    void updateBookingByIdForAuthUser_withValidInputData_returnResponseDto() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Bob");
        user.setLastName("User");

        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setAccommodationId(1L);
        requestDto.setCheckInDate(LocalDateTime.of(2024, 12, 29, 12, 0, 0));
        requestDto.setCheckOutDate(LocalDateTime.of(2025, 01, 01, 14, 0, 0));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setDailyRate(BigDecimal.valueOf(110));

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setAccommodation(accommodation);
        booking.setUser(user);
        booking.setStatus(Booking.Status.PENDING);
        booking.setCheckInDate(LocalDateTime.of(2024, 12, 29, 12, 0, 0));
        booking.setCheckOutDate(LocalDateTime.of(2024, 12, 31, 12, 0, 0));

        BookingResponseDto expected = new BookingResponseDto();
        expected.setId(booking.getId());
        expected.setAccommodationId(accommodation.getId());
        expected.setStatus(booking.getStatus().toString());
        expected.setUserName(user.getFirstName());
        expected.setCheckInDate(requestDto.getCheckInDate());
        expected.setCheckOutDate(requestDto.getCheckOutDate());

        Mockito.when(bookingRepository.findByUserIdAndId(user.getId(), booking.getId()))
                .thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.findByAccommodationId(1L)).thenReturn(List.of());
        Mockito.doNothing().when(bookingMapper).updateBookingFromDto(requestDto, booking);
        Mockito.when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        Mockito.when(bookingMapper.toDto(any(Booking.class))).thenReturn(expected);

        BookingResponseDto actual = bookingService.updateBookingByIdForAuthUser(
                user,
                requestDto,
                booking.getId()
        );

        assertEquals(expected.getAccommodationId(), actual.getAccommodationId());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getCheckInDate(), actual.getCheckInDate());
        assertEquals(expected.getCheckOutDate(), actual.getCheckOutDate());
        assertEquals(expected.getUserName(), actual.getUserName());
    }

    @Test
    @DisplayName("Update booking when exist overlapping booking`s date")
    void updateBookingByIdForAuthUser_withOverlappingDate_throwException() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Bob");
        user.setLastName("User");

        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setAccommodationId(1L);
        requestDto.setCheckInDate(LocalDateTime.of(2024, 12, 27, 12, 0, 0));
        requestDto.setCheckOutDate(LocalDateTime.of(2024, 12, 31, 12, 0, 0));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setDailyRate(BigDecimal.valueOf(110));
        accommodation.setAvailability(1);

        Booking booking = new Booking();
        booking.setAccommodation(accommodation);
        booking.setId(2L);
        booking.setCheckInDate(LocalDateTime.of(2024, 12, 27, 12, 0, 0));
        booking.setCheckOutDate(LocalDateTime.of(2024, 12, 29, 12, 0, 0));

        Booking overlappingBooking = new Booking();
        overlappingBooking.setAccommodation(accommodation);
        overlappingBooking.setId(3L);
        overlappingBooking.setCheckInDate(LocalDateTime.of(2024, 12, 30, 12, 0, 0));
        overlappingBooking.setCheckOutDate(LocalDateTime.of(2025, 01, 01, 12, 0, 0));

        Long bookingId = 1L;

        Mockito.when(bookingRepository.findByUserIdAndId(anyLong(), anyLong()))
                .thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.findByAccommodationId(anyLong()))
                .thenReturn(List.of(overlappingBooking));
        Mockito.when(accommodationRepository.findById(accommodation.getId()))
                .thenReturn(Optional.of(accommodation));

        assertThatThrownBy(() -> bookingService.updateBookingByIdForAuthUser(
                user,
                requestDto,
                bookingId
        ))
                .isInstanceOf(BookingAvailabilityException.class)
                .hasMessageContaining(String.format("Accommodation is booked from %s to %s.",
                        overlappingBooking.getCheckInDate(), overlappingBooking.getCheckOutDate()));
    }

    @Test
    @DisplayName("Returns all booking filter status or user id for admin")
    void getBookingByUserIdAndStatusForAdmin_withValidParameters_returnListBookings() {
        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus("PENDING");

        Booking booking = new Booking();
        booking.setId(responseDto.getId());
        booking.setStatus(Booking.Status.PENDING);

        String[] statusArray = new String[] {"PENDING"};
        String[] userIdArray = new String[0];
        BookingFilterParameters parameters = new BookingFilterParameters(statusArray,
                userIdArray);
        Specification<Booking> mockSpecification = Mockito.mock(Specification.class);

        List<BookingResponseDto> expected = List.of(responseDto);

        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> bookings = List.of(booking);
        Page<Booking> bookingPage = new PageImpl<>(bookings, pageable, bookings.size());

        Mockito.when(specificationBuilder.build(parameters)).thenReturn(mockSpecification);
        Mockito.when(bookingRepository.findAll(mockSpecification, pageable))
                .thenReturn(bookingPage);
        Mockito.when(bookingMapper.toListDto(bookings)).thenReturn(expected);

        List<BookingResponseDto> actual = bookingService.getAllBookingByUserIdAndStatus(
                parameters,
                pageable
        );

        assertEquals(expected, actual);
    }
}
