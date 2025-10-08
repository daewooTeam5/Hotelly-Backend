package daewoo.team5.hotelreservation.domain.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import daewoo.team5.hotelreservation.domain.auth.dto.GoogleUserInfo;
import daewoo.team5.hotelreservation.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    /**
     * Authorization code로 Access Token 받기
     */
    public String getAccessToken(String code, String redirectUri) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                GOOGLE_TOKEN_URL,
                request,
                String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();

        } catch (Exception e) {
            log.error("Google access token 발급 실패", e);
            throw new ApiException(500, "Google 로그인 실패", "액세스 토큰 발급에 실패했습니다.");
        }
    }

    /**
     * Access Token으로 사용자 정보 가져오기
     */
    public GoogleUserInfo getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                GOOGLE_USERINFO_URL,
                HttpMethod.GET,
                request,
                String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            GoogleUserInfo userInfo = new GoogleUserInfo();
            userInfo.setSub(jsonNode.get("sub").asText());
            userInfo.setEmail(jsonNode.get("email").asText());
            userInfo.setName(jsonNode.get("name").asText());
            userInfo.setPicture(jsonNode.has("picture") ? jsonNode.get("picture").asText() : null);
            userInfo.setEmailVerified(jsonNode.has("email_verified") ? jsonNode.get("email_verified").asBoolean() : false);

            return userInfo;

        } catch (Exception e) {
            log.error("Google 사용자 정보 조회 실패", e);
            throw new ApiException(500, "Google 로그인 실패", "사용자 정보 조회에 실패했습니다.");
        }
    }
}

