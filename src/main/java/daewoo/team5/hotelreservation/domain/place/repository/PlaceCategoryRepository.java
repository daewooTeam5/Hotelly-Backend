package daewoo.team5.hotelreservation.domain.place.repository;


import daewoo.team5.hotelreservation.domain.place.entity.PlaceCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceCategoryRepository extends JpaRepository<PlaceCategoryEntity, Integer> {
}
