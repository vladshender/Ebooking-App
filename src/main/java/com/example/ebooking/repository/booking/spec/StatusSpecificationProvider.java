package com.example.ebooking.repository.booking.spec;

import com.example.ebooking.model.Booking;
import com.example.ebooking.repository.SpecificationProvider;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class StatusSpecificationProvider implements SpecificationProvider<Booking> {
    @Override
    public String getKey() {
        return "status";
    }

    @Override
    public Specification<Booking> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) -> root.get("status")
                .in(Arrays.stream(params).toArray());
    }
}
