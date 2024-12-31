package com.example.ebooking.mapper;

import com.example.ebooking.config.MapperConfig;
import com.example.ebooking.dto.payment.CreatePaymentSessionDto;
import com.example.ebooking.dto.payment.PaymentResponseDto;
import com.example.ebooking.dto.payment.PaymentWithoutSessionDto;
import com.example.ebooking.model.Payment;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    @Mapping(source = "booking.id", target = "bookingId")
    List<PaymentResponseDto> toDtoList(List<Payment> payments);

    CreatePaymentSessionDto toPaymentResponseDto(Payment payment);

    @Mapping(source = "booking.id", target = "bookingId")
    PaymentWithoutSessionDto toPaymentWithoutSessionDto(Payment payment);
}
