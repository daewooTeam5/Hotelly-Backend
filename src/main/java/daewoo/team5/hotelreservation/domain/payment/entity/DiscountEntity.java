package daewoo.team5.hotelreservation.domain.payment.entity;

import daewoo.team5.hotelreservation.domain.place.entity.PlacesEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Discount")
@Table(name = "discount", indexes = {
        @Index(name = "idx_discount_placeid_date", columnList = "place_id, start_date, end_date")
})
public class DiscountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;
    private String name;
    private Integer discountValue;
    private Integer maxDiscountAmount;
    @ManyToOne
    private PlacesEntity place;
}