package com.example.ebooking.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.ebooking.dto.accommodation.AccommodationRequestDto;
import com.example.ebooking.dto.accommodation.AccommodationResponseDto;
import com.example.ebooking.exception.EntityNotFoundException;
import com.example.ebooking.mapper.AccommodationMapper;
import com.example.ebooking.model.Accommodation;
import com.example.ebooking.repository.accommodation.AccommodationRepository;
import com.example.ebooking.service.accommodation.AccommodationServiceImpl;
import com.example.ebooking.service.notification.TelegramNotificationService;
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

@ExtendWith(MockitoExtension.class)
public class AccommodationServiceTest {
    @InjectMocks
    private AccommodationServiceImpl accommodationService;

    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private AccommodationMapper accommodationMapper;
    @Mock
    private TelegramNotificationService notificationService;

    @Test
    @DisplayName("Returns list accommodation from database")
    void getAll_ValidPageable_returnAllAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setType(Accommodation.Type.HOUSE);
        accommodation.setLocation("Lviv, Shevchenko street, 17");

        AccommodationResponseDto responseDto = new AccommodationResponseDto();
        responseDto.setId(accommodation.getId());
        responseDto.setType(accommodation.getType().toString());
        responseDto.setLocation(accommodation.getLocation());

        Pageable pageable = PageRequest.of(0, 10);
        List<Accommodation> expected = List.of(accommodation);
        Page<Accommodation> accommodationPage = new PageImpl<>(expected, pageable, expected.size());

        Mockito.when(accommodationRepository.findAll(pageable)).thenReturn(accommodationPage);
        Mockito.when(accommodationMapper.toListDto(expected)).thenReturn(List.of(responseDto));

        List<AccommodationResponseDto> actual = accommodationService.getAll(pageable);

        assertEquals(expected.size(), actual.size());
    }

    @Test
    @DisplayName("Returns accommodation by id with valid id")
    void getAccommodationById_withValidId_returnAccommodation() {
        Long accommodationId = 1L;
        Accommodation accommodation = new Accommodation();
        accommodation.setId(accommodationId);
        accommodation.setType(Accommodation.Type.HOUSE);
        accommodation.setLocation("Lviv, Shevchenko street, 17");

        AccommodationResponseDto expected = new AccommodationResponseDto();
        expected.setId(accommodation.getId());
        expected.setType(accommodation.getType().toString());
        expected.setLocation(accommodation.getLocation());

        Mockito.when(accommodationRepository.findById(accommodationId))
                .thenReturn(Optional.of(accommodation));
        Mockito.when(accommodationMapper.toDto(accommodation)).thenReturn(expected);

        AccommodationResponseDto actual = accommodationService
                .getAccommodationById(accommodationId);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Returns accommodation by id with not valid id")
    void getAccommodationById_withNotValidId_throwException() {
        Long accommodationId = 1L;

        Mockito.when(accommodationRepository.findById(accommodationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> accommodationService.getAccommodationById(accommodationId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Can`t find accommodation by id: "
                        + accommodationId);
    }

    @Test
    @DisplayName("Save accommodation with valid request dto")
    void save_withValidRequestDto_returnAccommodation() {
        AccommodationRequestDto requestDto = new AccommodationRequestDto();
        requestDto.setType("HOUSE");

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setType(Accommodation.Type.HOUSE);

        AccommodationResponseDto expected = new AccommodationResponseDto();
        expected.setId(accommodation.getId());
        expected.setType("HOUSE");

        Mockito.when(accommodationMapper.toModel(requestDto)).thenReturn(accommodation);
        Mockito.when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        Mockito.when(accommodationMapper.toDto(accommodation)).thenReturn(expected);

        AccommodationResponseDto actual = accommodationService.save(requestDto);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update accommodation with valid request dto")
    void update_withValidDtoAndId_returnUpdatedAccommodation() {
        Long accommodationId = 1L;
        AccommodationRequestDto requestDto = new AccommodationRequestDto();
        requestDto.setLocation("Lviv, Shevchenko street, 23");

        Accommodation accommodation = new Accommodation();
        accommodation.setId(accommodationId);
        accommodation.setLocation("Lviv, Shevchenko street, 17");

        AccommodationResponseDto expected = new AccommodationResponseDto();
        expected.setLocation("Lviv, Shevchenko street, 23");
        expected.setId(accommodation.getId());

        Mockito.when(accommodationRepository.findById(accommodationId))
                .thenReturn(Optional.of(accommodation));
        Mockito.when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        Mockito.when(accommodationMapper.toDto(accommodation)).thenReturn(expected);

        AccommodationResponseDto actual = accommodationService.update(requestDto, accommodationId);
        assertEquals(expected.getLocation(), actual.getLocation());
    }

    @Test
    @DisplayName("Update accommodation with not valid accommodation id")
    void update_withNotValidId_throwException() {
        Long accommodationId = 1L;
        AccommodationRequestDto requestDto = new AccommodationRequestDto();
        requestDto.setLocation("Lviv, Shevchenko street, 23");

        Mockito.when(accommodationRepository.findById(accommodationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> accommodationService.update(requestDto, accommodationId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Can`t find accommodation by id: " + accommodationId);
    }
}
