package com.example.ebooking.service.accommodation;

import com.example.ebooking.dto.accommodation.AccommodationRequestDto;
import com.example.ebooking.dto.accommodation.AccommodationResponseDto;
import com.example.ebooking.exception.EntityNotFoundException;
import com.example.ebooking.mapper.AccommodationMapper;
import com.example.ebooking.model.Accommodation;
import com.example.ebooking.repository.accommodation.AccommodationRepository;
import com.example.ebooking.service.notification.TelegramNotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final TelegramNotificationService notificationService;

    @Override
    public List<AccommodationResponseDto> getAll(Pageable pageable) {
        return accommodationMapper.toListDto(accommodationRepository.findAll(pageable)
                .getContent());
    }

    @Override
    public AccommodationResponseDto getAccommodationById(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can`t find accommodation by id: " + id)
                );
        return accommodationMapper.toDto(accommodation);
    }

    @Override
    public AccommodationResponseDto save(AccommodationRequestDto requestDto) {
        Accommodation accommodation = accommodationMapper.toModel(requestDto);

        Accommodation savedAccommodation = accommodationRepository.save(accommodation);
        notificationService.sendAccommodationCreateMessage(savedAccommodation);

        return accommodationMapper.toDto(savedAccommodation);
    }

    @Transactional
    @Override
    public AccommodationResponseDto update(AccommodationRequestDto requestDto, Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can`t find accommodation by id: " + id)
                );
        accommodationMapper.updateAccommodationFromDto(requestDto, accommodation);
        return accommodationMapper.toDto(accommodationRepository.save(accommodation));
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        accommodationRepository.deleteById(id);
    }
}
