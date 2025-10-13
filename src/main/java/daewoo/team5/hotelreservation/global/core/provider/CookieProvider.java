package daewoo.team5.hotelreservation.global.core.provider;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class CookieProvider {
    @Value("${MODE:development}") private String mode;
    private static final String REFRESH_TOKEN = "refreshToken";
    private final JwtProvider jwtProvider;


    public void removeCookie(String name,HttpServletResponse response) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
    public ResponseCookie generateRefreshTokenCookie(String refreshToken) {
        boolean isProd = mode.equals("production");
        log.info("123mode: {}, isProd: {}", mode, isProd);
        Claims claims = jwtProvider.parseClaims(refreshToken);
        // 만료시간 - 현재시간 = 쿠키 만료시간
        long exp = claims.getExpiration().getTime()/1000;
        int cookieMaxAge = (int)(exp - (System.currentTimeMillis()/1000));

        return ResponseCookie.from(REFRESH_TOKEN, refreshToken)
                .httpOnly(true)
                .secure(isProd) // 운영 환경에서는 true로 설정
                .path("/")
                .maxAge(cookieMaxAge)
                .sameSite(isProd?"None":"Lax") // 같은 도메인에선 Lax, 다른 도메인에선 None
                .build();
    }
}
