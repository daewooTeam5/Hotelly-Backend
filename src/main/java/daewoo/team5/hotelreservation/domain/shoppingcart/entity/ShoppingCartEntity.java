package daewoo.team5.hotelreservation.domain.shoppingcart.entity;

import daewoo.team5.hotelreservation.domain.place.entity.RoomEntity;
import daewoo.team5.hotelreservation.domain.users.entity.UsersEntity;
import daewoo.team5.hotelreservation.global.core.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity(name = "ShoppingCart")
@Table(
        name = "shopping",   // 실제 DB 테이블명
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "room_id"})
        }
)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UsersEntity user;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;

    @Column(name = "check_in")
    private LocalDate startDate;

    @Column(name = "check_out")
    private LocalDate endDate;

    @Column(name = "quantity")
    private int quantity;
}