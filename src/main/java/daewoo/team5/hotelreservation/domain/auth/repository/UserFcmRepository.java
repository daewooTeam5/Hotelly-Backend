package daewoo.team5.hotelreservation.domain.auth.repository;

import daewoo.team5.hotelreservation.domain.auth.entity.UserFcmEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFcmRepository extends JpaRepository<UserFcmEntity,Long> {
    Optional<UserFcmEntity> findByUserId(Long userId);
    Optional<UserFcmEntity> findByUserIdAndDeviceType(Long userId, String deviceType);
    Optional<UserFcmEntity> findByToken(String token);
}
