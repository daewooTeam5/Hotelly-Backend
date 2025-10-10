package daewoo.team5.hotelreservation.domain.place.dto;

import daewoo.team5.hotelreservation.domain.payment.entity.GuestEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResultDto {
    private GuestEntity guest;
    private String placeName;
    private String roomBedType;
    private String roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
