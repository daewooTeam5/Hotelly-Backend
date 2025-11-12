package daewoo.team5.hotelreservation.global.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import daewoo.team5.hotelreservation.global.core.filter.HttpRequestEndPointChecker;
import daewoo.team5.hotelreservation.global.exception.ErrorDetails;
import daewoo.team5.hotelreservation.global.core.common.ApiResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final HttpRequestEndPointChecker endpointChecker;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        boolean endpointExists = endpointChecker.isEndpointExist(request);

        response.setContentType("application/json;charset=UTF-8");

        if (!endpointExists) {
            ErrorDetails errorDetails = new ErrorDetails(
                    null,
                    "Not Found",
                    404,
                    "요청하신 경로를 찾을 수 없습니다.",
                    request.getRequestURI()
            );

            ApiResult<?> notFoundResponse = ApiResult.builder()
                    .status(404)
                    .message("Not Found")
                    .error(errorDetails)
                    .success(false)
                    .timestamp(LocalDateTime.now())
                    .build();

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(objectMapper.writeValueAsString(notFoundResponse));
            return;
        }

        ErrorDetails errorDetails = new ErrorDetails(
                null,
                "Unauthorized",
                401,
                "인증되지 않은 사용자입니다. 로그인 후 이용해주세요.",
                request.getRequestURI()
        );

        ApiResult<?> unauthorized = ApiResult.builder()
                .status(401)
                .message("Unauthorized")
                .error(errorDetails)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(unauthorized));
    }
}
