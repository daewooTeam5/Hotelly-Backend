package daewoo.team5.hotelreservation.domain.place.entity;

import daewoo.team5.hotelreservation.global.core.common.BaseCUTimeEntity;
import daewoo.team5.hotelreservation.global.core.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity(name = "room")
@Table(name = "room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomEntity extends BaseCUTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 방 ID

    @ManyToOne
    private PlacesEntity place; // 숙소 ID (FK: Place 테이블)

    @Column(name = "room_name", length = 200, nullable = false)
    private String roomName;

    @Column(name = "room_type", length = 100)
    private String roomType; // 방 유형

    @Column(name = "bed_type", length = 100)
    private String bedType; // 침대 유형

    @Column(name = "capacity_people", nullable = false)
    private Integer capacityPeople; // 수용 가능 인원

    @Builder.Default
    @Column(name = "capacity_room", nullable = false)
    private Integer capacityRoom = 1;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price; // 가격

    @Column(name = "area")
    private Double area; // 면적

}
