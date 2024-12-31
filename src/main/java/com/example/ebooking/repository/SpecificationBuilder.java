package com.example.ebooking.repository;

import com.example.ebooking.dto.booking.BookingFilterParameters;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T> {
    Specification<T> build(BookingFilterParameters parameters);
}
