package daewoo.team5.hotelreservation.domain.place.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "PlaceCategory")
@Table(name = "place_category")
@Getter
@Setter
@NoArgsConstructor
public class PlaceCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
