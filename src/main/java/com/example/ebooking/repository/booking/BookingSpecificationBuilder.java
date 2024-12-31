package com.example.ebooking.repository.booking;

import com.example.ebooking.dto.booking.BookingFilterParameters;
import com.example.ebooking.model.Booking;
import com.example.ebooking.repository.SpecificationBuilder;
import com.example.ebooking.repository.SpecificationProviderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookingSpecificationBuilder implements SpecificationBuilder<Booking> {
    private final SpecificationProviderManager<Booking> specificationProviderManager;

    @Override
    public Specification<Booking> build(BookingFilterParameters parameters) {
        Specification<Booking> spec = Specification.where(null);
        if (parameters.statusArray() != null && parameters.userIdArray().length > 0) {
            spec = spec.and(specificationProviderManager.getSpecificationProvider("userId")
                    .getSpecification(parameters.userIdArray()));
        }
        if (parameters.userIdArray() != null && parameters.statusArray().length > 0) {
            spec = spec.and(specificationProviderManager.getSpecificationProvider("status")
                    .getSpecification(parameters.statusArray()));
        }
        return spec;
    }
}
