package com.example.ebooking.service.booking;

import com.example.ebooking.dto.booking.BookingFilterParameters;
import com.example.ebooking.dto.booking.BookingRequestDto;
import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.dto.booking.UpdateBookingStatusRequestDto;
import com.example.ebooking.exception.BookingAvailabilityException;
import com.example.ebooking.exception.EntityNotFoundException;
import com.example.ebooking.exception.PendingPaymentException;
import com.example.ebooking.mapper.BookingMapper;
import com.example.ebooking.model.Accommodation;
import com.example.ebooking.model.Booking;
import com.example.ebooking.model.User;
import com.example.ebooking.repository.accommodation.AccommodationRepository;
import com.example.ebooking.repository.booking.BookingRepository;
import com.example.ebooking.repository.booking.BookingSpecificationBuilder;
import com.example.ebooking.repository.user.UserRepository;
import com.example.ebooking.service.notification.TelegramNotificationService;
import com.example.ebooking.service.payment.StripePaymentService;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final TelegramNotificationService notificationService;
    private final StripePaymentService paymentService;

    @Override
    @Transactional
    public BookingResponseDto save(User user, BookingRequestDto requestDto) {
        if (paymentService.existsByBookingUserIdAndStatus(user.getId())) {
            throw new PendingPaymentException("The user has unpaid reservations!");
        }

        Accommodation accommodation = checkDateOverlappingAndAvailabilityForSave(requestDto);
        User userFromDB = userRepository.findById(user.getId()).orElseThrow(
                () -> new EntityNotFoundException("Can`t find user by id: " + user.getId())
        );

        Booking booking = bookingMapper.toModel(requestDto);
        booking.setAccommodation(accommodation);
        booking.setUser(userFromDB);
        booking.setStatus(Booking.Status.PENDING);

        Booking savedBooking = bookingRepository.save(booking);
        notificationService.sendBookingCreateMessage(accommodation, userFromDB, savedBooking);

        return bookingMapper.toDto(savedBooking);
    }

    @Override
    public List<BookingResponseDto> getAllBookingsByUser(User user,
                                                         Pageable pageable) {
        Page<Booking> bookingsFromDB = bookingRepository.findByUserId(user.getId(),
                pageable);

        if (bookingsFromDB.getContent().isEmpty()) {
            throw new EntityNotFoundException("Can`t find bookings "
                    + "by user id: " + user.getId());
        }

        return bookingMapper.toListDto(bookingsFromDB.getContent());
    }

    @Override
    public BookingResponseDto getBookingByIdForUser(User user, Long id) {
        Booking bookingFromDB = getBookingByIdForAuthUser(user, id);
        return bookingMapper.toDto(bookingFromDB);
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingByIdForAuthUser(
            User user,
            BookingRequestDto requestDto,
            Long id) {
        Booking bookingFromDB = getBookingByIdForAuthUser(user, id);
        checkDateOverlappingAndAvalaibilityForUpdate(bookingFromDB, requestDto);
        bookingMapper.updateBookingFromDto(requestDto, bookingFromDB);
        return bookingMapper.toDto(bookingRepository.save(bookingFromDB));
    }

    @Override
    @Transactional
    public void canceledById(User user, Long id) {
        Booking booking = bookingRepository.findByUserIdAndId(user.getId(), id)
                .orElseThrow(
                        () -> new EntityNotFoundException("The user does not have a "
                                + "reservation with id: " + id)
                );
        bookingRepository.updateStatus(id, Booking.Status.CANCELED);
        booking.setStatus(Booking.Status.CANCELED);
        Accommodation accommodation = booking.getAccommodation();
        notificationService.sendBookingCanceledMessage(accommodation, user, booking);
    }

    @Override
    @Transactional
    public void deleteById(User user, Long id) {
        Booking booking = bookingRepository.findByUserIdAndId(user.getId(), id)
                .orElseThrow(
                        () -> new EntityNotFoundException("The user does not have a "
                                + "reservation with id: " + id)
                );
        bookingRepository.deleteById(id);
    }

    @Override
    public List<BookingResponseDto> getAllBookingByUserIdAndStatus(
            BookingFilterParameters parameters,
            Pageable pageable) {
        Specification<Booking> specification = specificationBuilder.build(parameters);

        Page<Booking> bookingPage = bookingRepository.findAll(specification, pageable);

        List<BookingResponseDto> responseDtoList = bookingMapper.toListDto(bookingPage
                .getContent());

        if (responseDtoList.isEmpty()) {
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
        booking.setStatus(Booking.Status.valueOf(requestDto.getStatus()));
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    @Override
    public void checkHourlyExpiredBookings() {
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

        List<Booking> bookingExpiredList = bookingRepository.findByCheckOutDateAndStatusNot(now,
                Booking.Status.CANCELED);

        if (!bookingExpiredList.isEmpty()) {
            setBookingStatusToExpired(bookingExpiredList);

            Map<Accommodation, Long> expiringBookingsByAccommodation = bookingExpiredList.stream()
                    .collect(Collectors.groupingBy(
                            Booking::getAccommodation,
                            Collectors.counting())
                    );

            List<Integer> amountOfAvailability = expiringBookingsByAccommodation.entrySet().stream()
                    .map(entry -> {
                        Accommodation accommodation = entry.getKey();
                        long bookedCount = entry.getValue();
                        long totalBookings = bookingRepository.findByAccommodationId(
                                accommodation.getId()
                        ).size();
                        return (int) (accommodation.getAvailability()
                                - (totalBookings - bookedCount));
                    })
                    .collect(Collectors.toList());

            notificationService.sendAccommodationReleaseMessage(expiringBookingsByAccommodation,
                    amountOfAvailability);
        }
    }

    private Accommodation checkDateOverlappingAndAvailabilityForSave(
            BookingRequestDto requestDto) {
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
            BookingRequestDto requestDto) {
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

    private Boolean isOverlapping(Booking booking, BookingRequestDto requestDto) {
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

    private void setBookingStatusToExpired(List<Booking> bookings) {
        Set<Long> bookingIds = new HashSet<>();

        for (Booking booking : bookings) {
            bookingIds.add(booking.getId());
        }
        bookingRepository.updateStatusForExpiredBooking(bookingIds,
                Booking.Status.EXPIRED);
    }
}
