package com.example.ebooking.repository.booking;

import com.example.ebooking.model.Booking;
import com.example.ebooking.repository.SpecificationProvider;
import com.example.ebooking.repository.SpecificationProviderManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookingSpecificationProviderManager implements SpecificationProviderManager {
    private final List<SpecificationProvider<Booking>> specificationProviderList;

    @Override
    public SpecificationProvider getSpecificationProvider(String key) {
        return specificationProviderList.stream()
                .filter(b -> b.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can`t find correct specification for key: "
                        + key));
    }
}
