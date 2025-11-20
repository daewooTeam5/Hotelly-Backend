package daewoo.team5.hotelreservation.global.handler;


import daewoo.team5.hotelreservation.global.exception.ApiException;
import daewoo.team5.hotelreservation.global.exception.ErrorDetails;
import daewoo.team5.hotelreservation.global.core.common.ApiResult;
import daewoo.team5.hotelreservation.infrastructure.webhook.DiscordNotifier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class CustomApiHandler implements ResponseBodyAdvice<Object> {
    private final DiscordNotifier discordNotifier;


    @Value("${MODE:production}")
    private String mode;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<?>> handleException(
            Exception e,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.error("Exception: ", e);
        ErrorDetails errorDetails = new ErrorDetails(
                null,
                "서버 오류",
                500,
                e.getMessage(),
                request.getRequestURI()
        );
        if ("production".equals(mode)) {
            sendErrorToDiscord(request, response, e.getMessage(), e);
        }
        return ResponseEntity.status(500).body(
                ApiResult.builder()
                        .message("서버 오류")
                        .success(false)
                        .timestamp(LocalDateTime.now())
                        .error(errorDetails)
                        .build()
        );
    }

    @ExceptionHandler(ApiException.class)
    public ApiResult<?> handleApiException(ApiException e, HttpServletRequest request, HttpServletResponse response) {
        ErrorDetails error = e.getError();
        log.error(error.toString());
        if (error.getInstance() == null) {
            error.setInstance(request.getRequestURI());
        }
        response.setStatus(error.getStatus());
        log.error(e.toString());

        if ("production".equals(mode)) {
            sendErrorToDiscord(request, response, e.getMessage(), e);
        }
        return
                ApiResult.builder()
                        .message(error.getTitle())
                        .success(false)
                        .timestamp(LocalDateTime.now())
                        .error(error)
                        .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<?>> handleValidationException(MethodArgumentNotValidException ex,
                                                                  HttpServletRequest request) {
        log.error("Validation Error: ", ex);

        // 필드별 에러 메시지 맵핑
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                validationErrors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorDetails errorDetails = new ErrorDetails(
                null,
                "검증 오류",
                400,
                validationErrors.toString(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(
                ApiResult.builder()
                        .message("입력값 검증 실패")
                        .success(false)
                        .timestamp(LocalDateTime.now())
                        .error(errorDetails)
                        .build()
        );
    }

    // 특정 타입에서만 beforeBodyWrite 가 작동하도록 설정
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType().equals(ApiResult.class);
    }

    // 응답전에 바디 가공 APIResult 타입의 응답코드를 실제 response 에 설정
    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        // 응답 타입이 ApiResult 인지 검증
        if (body instanceof ApiResult) {
            ApiResult<?> apiResult = (ApiResult<?>) body;
            // api 요청 성공시에만 상태코드 설정 (실패는 ExceptionHandler 에서 처리)
            if (apiResult.getSuccess()) {
                response.setStatusCode(HttpStatus.valueOf(apiResult.getStatus()));
            }
            return body;
        }
        return body;
    }
    private void sendErrorToDiscord(HttpServletRequest request, HttpServletResponse response, String title, Exception e) {
        try {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) ip = request.getRemoteAddr();

            String userAgent = request.getHeader("User-Agent");
            String deviceInfo = discordNotifier.parseDeviceInfo(userAgent);

            Map<String, String> info = new HashMap<>();
            info.put("URL", request.getRequestURI());
            info.put("발생 시간", LocalDateTime.now().toString());
            info.put("IP 주소", ip);
            info.put("디바이스", deviceInfo);
            info.put("상태코드", HttpStatus.valueOf(response.getStatus()).toString());
            info.put("에러 제목", title);
            info.put("예외 전체 메시지", e.toString());
            info.put("에러 메시지", formatStackTrace(e));

            log.info(info.toString());
            discordNotifier.sendError("🔥 ApiException 발생", info);

        } catch (Exception ex) {
            log.error("⚠️ Discord 전송 중 오류 발생", ex);
        }
    }

    // ✅ 스택 트레이스를 문자열로 변환 + 길이 제한 (디스코드 메시지 제한 고려)
    private String formatStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String fullTrace = sw.toString();

        // 너무 길면 잘라서 생략 처리
        if (fullTrace.length() > 1800) {
            return "```java\n" + fullTrace.substring(0, 1800) + "\n... (생략됨) ...```";
        }
        return "```java\n" + fullTrace + "```";
    }
}
