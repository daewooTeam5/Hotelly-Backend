package daewoo.team5.hotelreservation.domain.auth.repository;


import daewoo.team5.hotelreservation.global.core.provider.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;

@RequiredArgsConstructor
@Repository
public class WhiteListRepository {
    private final static String ACCOUNT_KEY_PREFIX = "account:";
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProvider jwtProvider;

    public void upsertToken(String token, Long accountId, String device) {
        Claims claims;
        try {
            claims = jwtProvider.parseClaims(token);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JWT token", ex);
        }

        Date expiration = claims.getExpiration();
        long ttl = expiration.getTime() - System.currentTimeMillis(); // milliseconds

        if (ttl <= 0) {
            throw new IllegalArgumentException("JWT is already expired");
        }

        Duration duration = Duration.ofMillis(ttl);

        String key = ACCOUNT_KEY_PREFIX + device + ":" + accountId;

        // Check existing value (to determine insert vs update; both handled by set)
        String existing = redisTemplate.opsForValue().get(key);

        String newToken = generateToken();

        // Upsert with TTL (set overwrites existing value and applies TTL)
        redisTemplate.opsForValue().set(key, newToken, duration);
    }

    public Boolean validateToken(Long accountId, String device, String token) {
        String key = ACCOUNT_KEY_PREFIX + device + ":" + accountId;
        String stored = redisTemplate.opsForValue().get(key);

        if (stored == null) {
            return Boolean.FALSE;
        }

        return stored.equals(token) ? Boolean.TRUE : Boolean.FALSE;

    }

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];  // 256bit
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
