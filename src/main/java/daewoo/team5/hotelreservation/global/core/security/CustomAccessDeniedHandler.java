package daewoo.team5.hotelreservation.global.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import daewoo.team5.hotelreservation.global.core.filter.HttpRequestEndPointChecker;
import daewoo.team5.hotelreservation.global.exception.ErrorDetails;
import daewoo.team5.hotelreservation.global.core.common.ApiResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final HttpRequestEndPointChecker endpointChecker;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
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
                "Forbidden",
                403,
                "접근 권한이 없습니다.",
                request.getRequestURI()
        );

        ApiResult<?> forbiddenResponse = ApiResult.builder()
                .status(403)
                .message("Forbidden")
                .error(errorDetails)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(objectMapper.writeValueAsString(forbiddenResponse));
    }
}
