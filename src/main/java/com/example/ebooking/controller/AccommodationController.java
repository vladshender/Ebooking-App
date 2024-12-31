package com.example.ebooking.controller;

import com.example.ebooking.dto.accommodation.AccommodationRequestDto;
import com.example.ebooking.dto.accommodation.AccommodationResponseDto;
import com.example.ebooking.service.accommodation.AccommodationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Accommodation management", description = "Endpoints for accommodation")
@RestController
@RequiredArgsConstructor
@RequestMapping("/accommodations")
public class AccommodationController {
    private final AccommodationService accommodationService;

    @Operation(summary = "Get all accommodation",
            description = " Get all accommodation for non authentication user")
    @GetMapping("/list")
    public List<AccommodationResponseDto> getAll(Pageable pageable) {
        return accommodationService.getAll(pageable);
    }

    @Operation(summary = "Get accommodation by id",
            description = " Get accommodation by id for authentication user")
    @GetMapping("/{id}")
    public AccommodationResponseDto getAccommodationById(@PathVariable Long id) {
        return accommodationService.getAccommodationById(id);
    }

    @Operation(summary = "Create new accommodation",
            description = "Create new accommodation for admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public AccommodationResponseDto save(@RequestBody @Valid AccommodationRequestDto requestDto) {
        return accommodationService.save(requestDto);
    }

    @Operation(summary = "Update accommodation by id",
            description = "Update accommodation by id for admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public AccommodationResponseDto update(@RequestBody @Valid AccommodationRequestDto requestDto,
                                           @PathVariable Long id) {
        return accommodationService.update(requestDto, id);
    }

    @Operation(summary = "Delete accommodation by id",
            description = "Delete accommodation by id for admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        accommodationService.deleteById(id);
    }
}
