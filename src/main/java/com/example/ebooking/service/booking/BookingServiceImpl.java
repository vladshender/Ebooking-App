package com.example.ebooking.service.booking;

import com.example.ebooking.dto.booking.BookingFilterParameters;
import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.dto.booking.CreateAndUpdateBookingRequestDto;
import com.example.ebooking.dto.booking.UpdateBookingStatusRequestDto;
import com.example.ebooking.exception.BookingAvailabilityException;
import com.example.ebooking.exception.EntityNotFoundException;
import com.example.ebooking.mapper.BookingMapper;
import com.example.ebooking.model.Accommodation;
import com.example.ebooking.model.Booking;
import com.example.ebooking.model.User;
import com.example.ebooking.repository.accommodation.AccommodationRepository;
import com.example.ebooking.repository.booking.BookingRepository;
import com.example.ebooking.repository.booking.BookingSpecificationBuilder;
import com.example.ebooking.repository.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;
    private final BookingSpecificationBuilder specificationBuilder;

    @Override
    @Transactional
    public BookingResponseDto save(User user, CreateAndUpdateBookingRequestDto requestDto) {
        Accommodation accommodation = checkDateOverlappingAndAvailabilityForSave(requestDto);
        Booking booking = bookingMapper.toModel(requestDto);
        booking.setAccommodation(accommodation);
        User userFromDB = userRepository.findById(user.getId()).orElseThrow(
                    () -> new EntityNotFoundException("Can`t find user by id: " + user.getId())
        );
        booking.setUser(userFromDB);
        booking.setStatus(Booking.Status.PENDING);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public List<BookingResponseDto> getBookingsForAuthUser(User user) {
        List<Booking> bookingsFromDB = bookingRepository.findByUserId(user.getId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Can`t find bookings "
                                + "by user id: " + user.getId())
                );
        return bookingMapper.toListDto(bookingsFromDB);
    }

    @Override
    public BookingResponseDto getBookingByIdFotAuthUser(User user, Long id) {
        Booking bookingFromDB = getBookingByIdForAuthUser(user, id);
        return bookingMapper.toDto(bookingFromDB);
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingByIdForAuthUser(
            User user,
            CreateAndUpdateBookingRequestDto requestDto,
            Long id) {
        Booking bookingFromDB = getBookingByIdForAuthUser(user, id);
        checkDateOverlappingAndAvalaibilityForUpdate(bookingFromDB, requestDto);
        bookingMapper.updateBookingFromDto(requestDto, bookingFromDB);
        return bookingMapper.toDto(bookingRepository.save(bookingFromDB));
    }

    @Override
    @Transactional
    public void deleteById(User user, Long id) {
        Booking booking = bookingRepository.findByUserIdAndId(user.getId(), id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Booking with id "
                                + id
                                + " don`t exist")
                );
        bookingRepository.deleteById(id);
    }

    @Override
    public List<BookingResponseDto> getBookingByUserIdAndStatusForAdmin(
            BookingFilterParameters parameters) {
        Specification<Booking> specification = specificationBuilder.build(parameters);
        List<BookingResponseDto> responseDtoList = bookingMapper.toListDto(
                bookingRepository.findAll(specification));
        if (!responseDtoList.isEmpty()) {
            return responseDtoList;
        } else {
            System.out.println("Nothing was found for the specified filters");
        }
        return responseDtoList;
    }

    @Override
    @Transactional
    public BookingResponseDto updateStatusById(UpdateBookingStatusRequestDto requestDto,
                                               Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can`t find booking "
                        + "by id: " + id)
        );
        booking.setStatus(requestDto.getStatus());
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    private Accommodation checkDateOverlappingAndAvailabilityForSave(
            CreateAndUpdateBookingRequestDto requestDto) {
        List<Booking> bookingList = bookingRepository
                .findByAccommodationId(requestDto.getAccommodationId());
        List<Booking> overlappingBookings = bookingList.stream()
                .filter(b -> isOverlapping(b, requestDto))
                .collect(Collectors.toList());

        if (!overlappingBookings.isEmpty()) {
            boolean isAvailable = checkAvailability(requestDto.getAccommodationId(),
                    overlappingBookings.size())
                    .values().iterator().next();
            if (!isAvailable) {
                String messages = overlappingBookings.stream()
                        .map(booking -> String.format("Accommodation is booked from %s to %s.",
                                booking.getCheckInDate(), booking.getCheckOutDate()))
                        .collect(Collectors.joining("\n"));
                throw new BookingAvailabilityException("There are no available "
                        + "accommodations left for booking.\n" + messages);
            }
        }

        return getAccommodationFromDB(requestDto.getAccommodationId());
    }

    private Boolean checkDateOverlappingAndAvalaibilityForUpdate(
            Booking booking,
            CreateAndUpdateBookingRequestDto requestDto) {
        List<Booking> bookingList = bookingRepository
                .findByAccommodationId(requestDto.getAccommodationId());
        List<Booking> overlappingBookings = bookingList.stream()
                .filter(b -> isOverlapping(b, requestDto))
                .filter(b -> !b.getId().equals(booking.getId()))
                .collect(Collectors.toList());

        if (!overlappingBookings.isEmpty()) {
            boolean isAvailable = checkAvailability(requestDto.getAccommodationId(),
                    overlappingBookings.size())
                    .values().iterator().next();
            if (!isAvailable) {
                String messages = overlappingBookings.stream()
                        .map(b -> String.format("Accommodation is booked from %s to %s.",
                                b.getCheckInDate(), b.getCheckOutDate()))
                        .collect(Collectors.joining("\n"));
                throw new BookingAvailabilityException("There are no available "
                        + "accommodations left for booking.\n" + messages);
            }
        }
        return true;
    }

    private Map<Accommodation, Boolean> checkAvailability(Long accommodationId,
                                                          Integer numberOfMatches) {
        Accommodation accommodation = getAccommodationFromDB(accommodationId);
        boolean isAvailable = accommodation.getAvailability() > numberOfMatches;
        return Map.of(accommodation, isAvailable);
    }

    private Boolean isOverlapping(Booking booking, CreateAndUpdateBookingRequestDto requestDto) {
        LocalDateTime newBookingInDate = requestDto.getCheckInDate();
        LocalDateTime newBookingOutDate = requestDto.getCheckOutDate();
        LocalDateTime oldBookingInDate = booking.getCheckInDate();
        LocalDateTime oldBookingOutDate = booking.getCheckOutDate();

        boolean isStartInOverlap = newBookingInDate.isAfter(oldBookingInDate)
                && newBookingInDate.isBefore(oldBookingOutDate);
        boolean isStartEqualOverlap = newBookingInDate.equals(oldBookingInDate);
        boolean isEndInOverlap = newBookingOutDate.isAfter(oldBookingInDate)
                && newBookingOutDate.isBefore(oldBookingOutDate);
        boolean isEndEqualOverlap = newBookingOutDate.equals(oldBookingOutDate);

        return isStartInOverlap || isStartEqualOverlap || isEndInOverlap || isEndEqualOverlap;
    }

    private Accommodation getAccommodationFromDB(Long id) {
        return accommodationRepository.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can`t find accommodation by id: "
                                + id));
    }

    private Booking getBookingByIdForAuthUser(User user, Long id) {
        return bookingRepository.findByUserIdAndId(user.getId(), id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Booking with id "
                                + id
                                + " not found for user id: "
                                + user.getId())
                );
    }
}