package com.example.ebooking.mapper;

import com.example.ebooking.config.MapperConfig;
import com.example.ebooking.dto.booking.BookingRequestDto;
import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.model.Booking;
import com.example.ebooking.model.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class, uses = AccommodationMapper.class)
public interface BookingMapper {
    @Mapping(source = "accommodation.id", target = "accommodationId")
    @Mapping(source = "user", target = "userName", qualifiedByName = "setUserName")
    BookingResponseDto toDto(Booking booking);

    @Mapping(target = "accommodation", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    Booking toModel(BookingRequestDto requestDto);

    List<BookingResponseDto> toListDto(List<Booking> bookingList);

    void updateBookingFromDto(BookingRequestDto requestDto,
                              @MappingTarget Booking booking);

    @Named("setUserName")
    default String setUserName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
