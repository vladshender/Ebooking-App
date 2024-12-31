package com.example.ebooking.service.accommodation;

import com.example.ebooking.dto.accommodation.AccommodationRequestDto;
import com.example.ebooking.dto.accommodation.AccommodationResponseDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface AccommodationService {
    List<AccommodationResponseDto> getAll(Pageable pageable);

    AccommodationResponseDto getAccommodationById(Long id);

    AccommodationResponseDto save(AccommodationRequestDto requestDto);

    AccommodationResponseDto update(AccommodationRequestDto requestDto, Long id);

    void deleteById(Long id);
}
