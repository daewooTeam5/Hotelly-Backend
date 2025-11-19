package daewoo.team5.hotelreservation.domain.auth.repository;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.security.SecureRandom;
import java.util.Base64;

@RequiredArgsConstructor
@Repository
public class WhiteListRepository {
    private final RedisTemplate<String, String> redisTemplate;
    public void addToken(){

    }

    private String generateToken(){
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];  // 256bit
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
