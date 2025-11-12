package daewoo.team5.hotelreservation.domain.place.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity(name = "DailyPlaceReservation")
@Table(name = "daily_place_reservation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPlaceReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer availableRoom;
}
