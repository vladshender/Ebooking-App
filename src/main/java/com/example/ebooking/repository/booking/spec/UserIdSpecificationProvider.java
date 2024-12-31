package com.example.ebooking.repository.booking.spec;

import com.example.ebooking.model.Booking;
import com.example.ebooking.repository.SpecificationProvider;
import java.util.Arrays;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserIdSpecificationProvider implements SpecificationProvider<Booking> {
    @Override
    public String getKey() {
        return "userId";
    }

    @Override
    public Specification<Booking> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) -> {
            List<Long> userIds = Arrays.stream(params)
                    .map(Long::valueOf)
                    .toList();

            return root.get("user").get("id").in(userIds);
        };
    }
}
