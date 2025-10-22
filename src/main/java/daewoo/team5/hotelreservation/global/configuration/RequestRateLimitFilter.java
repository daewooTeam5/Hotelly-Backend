package daewoo.team5.hotelreservation.global.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;



import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RequestRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 3000;
    private static final long TIME_WINDOW_MS = 10 * 60 * 1000; // 10분
//10분에 20번 이상 요청을 보내면 발동되어 10분 동안 막아두기

    private final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        long now = Instant.now().toEpochMilli();

        RequestInfo info = requestCounts.getOrDefault(clientIp, new RequestInfo(0, now));

        if (now - info.startTime > TIME_WINDOW_MS) {
            // 윈도우 리셋
            info = new RequestInfo(1, now);
        } else {
            info.count++;
        }

        requestCounts.put(clientIp, info);

        if (info.count > MAX_REQUESTS) {
            response.setStatus(429);
            response.getWriter().write("요청이 너무 많아요. 나중에 다시 시도해주세요.");
            return ;
        }

        filterChain.doFilter(request, response);
    }

    private static class RequestInfo {
        int count;
        long startTime;

        RequestInfo(int count, long startTime) {
            this.count = count;
            this.startTime = startTime;
        }
    }
}
