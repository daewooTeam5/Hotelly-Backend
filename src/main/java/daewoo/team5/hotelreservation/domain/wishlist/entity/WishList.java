package daewoo.team5.hotelreservation.domain.wishlist.entity;

import daewoo.team5.hotelreservation.domain.place.entity.PlacesEntity;
import daewoo.team5.hotelreservation.domain.users.entity.UsersEntity;
import daewoo.team5.hotelreservation.global.core.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "wishlist",   // 실제 DB 테이블명
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "place_id"})
        },
        indexes = {
                @Index(name = "idx_wishlist_user_place", columnList = "user_id, place_id")
        }
)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishList extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UsersEntity user;

    @ManyToOne
    @JoinColumn(name = "place_id", nullable = false)
    private PlacesEntity place;
}