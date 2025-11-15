package daewoo.team5.hotelreservation.domain.place.repository;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ReservationEventRepository {
    private final RedisTemplate<String,String> redisTemplate;
    private static final String RESERVATION_PREFIX = "place:reservation:cancel:";

    // 20분뒤에 ttl 종료및 redis keyspace 에서 감지하여 예약 취소
    public void registerReservationCancelEvent(Long reservationId) {
        LocalDateTime now = LocalDateTime.now();
        redisTemplate.opsForValue().set(RESERVATION_PREFIX+reservationId,"reservation:"+reservationId, Duration.ofMinutes(20));
    }

}
