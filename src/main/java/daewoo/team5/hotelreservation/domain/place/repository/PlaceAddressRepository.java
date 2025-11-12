package daewoo.team5.hotelreservation.domain.place.repository;


import daewoo.team5.hotelreservation.domain.place.entity.PlaceAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceAddressRepository extends JpaRepository<PlaceAddressEntity, Long> {
    List<PlaceAddressEntity> findByPlaceId(Long placeId);


    void deleteByPlaceId(Long placeId);//삭제용

    Optional<PlaceAddressEntity> findFirstByPlaceId(Long placeId);
}
