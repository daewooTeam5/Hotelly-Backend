package daewoo.team5.hotelreservation.domain.place.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RoomUpdateDTO {
    private Integer roomNumber;

    private String roomType;

    private Integer capacityPeople;

    private Integer minPrice;

    private Integer extraPrice;

    private String bedType;


    private Integer capacityRoom;

    private List<Long> amenityIds;

}