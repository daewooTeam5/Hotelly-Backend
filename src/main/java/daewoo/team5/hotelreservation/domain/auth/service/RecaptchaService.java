package daewoo.team5.hotelreservation.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecaptchaService {

    @Value("${recaptcha.secret-key}")
    private String secretKey;

    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * reCAPTCHA 토큰을 검증합니다.
     * @param token 클라이언트에서 받은 reCAPTCHA 토큰
     * @return 검증 성공 여부
     */
    public boolean verifyToken(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("reCAPTCHA 토큰이 비어있습니다.");
            return false;
        }

        try {
            // 요청 바디 설정
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("secret", secretKey);
            body.add("response", token);

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            // Google reCAPTCHA API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    RECAPTCHA_VERIFY_URL,
                    request,
                    Map.class
            );

            if (response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");

                log.info("reCAPTCHA 검증 결과: {}", success);

                // 실패 시 에러 코드 로깅
                if (Boolean.FALSE.equals(success) && responseBody.containsKey("error-codes")) {
                    log.warn("reCAPTCHA 검증 실패 - 에러 코드: {}", responseBody.get("error-codes"));
                }

                // 추가 검증 정보 로깅 (reCAPTCHA v3의 경우)
                if (responseBody.containsKey("score")) {
                    Double score = (Double) responseBody.get("score");
                    log.info("reCAPTCHA Score: {}", score);

                    // v3의 경우 score가 0.5 이상이면 통과
                    if (score != null && score < 0.5) {
                        log.warn("reCAPTCHA Score가 낮습니다: {}", score);
                        return false;
                    }
                }

                if (responseBody.containsKey("action")) {
                    log.info("reCAPTCHA Action: {}", responseBody.get("action"));
                }

                if (responseBody.containsKey("challenge_ts")) {
                    log.info("reCAPTCHA Challenge Timestamp: {}", responseBody.get("challenge_ts"));
                }

                if (responseBody.containsKey("hostname")) {
                    log.info("reCAPTCHA Hostname: {}", responseBody.get("hostname"));
                }

                return Boolean.TRUE.equals(success);
            }

            log.error("reCAPTCHA 검증 응답이 비어있습니다.");
            return false;

        } catch (Exception e) {
            log.error("reCAPTCHA 검증 중 오류 발생", e);
            return false;
        }
    }

    /**
     * reCAPTCHA 토큰 검증 및 점수 반환 (v3용)
     * @param token 클라이언트에서 받은 reCAPTCHA 토큰
     * @return 검증 점수 (0.0 ~ 1.0), 실패 시 -1.0
     */
    public double verifyTokenWithScore(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("reCAPTCHA 토큰이 비어있습니다.");
            return -1.0;
        }

        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("secret", secretKey);
            body.add("response", token);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    RECAPTCHA_VERIFY_URL,
                    request,
                    Map.class
            );

            if (response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");

                if (Boolean.TRUE.equals(success) && responseBody.containsKey("score")) {
                    Double score = (Double) responseBody.get("score");
                    log.info("reCAPTCHA Score: {}", score);
                    return score != null ? score : -1.0;
                }
            }

            return -1.0;

        } catch (Exception e) {
            log.error("reCAPTCHA 검증 중 오류 발생", e);
            return -1.0;
        }
    }
}