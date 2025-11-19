package daewoo.team5.hotelreservation.domain.auth.repository;


import daewoo.team5.hotelreservation.domain.auth.entity.UserFcmEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FcmCacheRepository {
    private final String FCM_KEY_PREFIX = "fcm:";
    private final RedisTemplate<String, String> redisTemplate;

    public void saveFcmToken(Long userId, UserFcmEntity.DeviceType device, String fcmToken) {
        String key = FCM_KEY_PREFIX + userId + ":" + device.toString();
        redisTemplate.opsForValue().set(key, fcmToken);
    }

    public String getFcmToken(Long userId, UserFcmEntity.DeviceType device) {
        String key = FCM_KEY_PREFIX + userId + ":" + device.toString();
        return redisTemplate.opsForValue().get(key);
    }

}
