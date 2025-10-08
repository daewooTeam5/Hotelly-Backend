package daewoo.team5.hotelreservation.domain.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import daewoo.team5.hotelreservation.domain.auth.dto.KakaoUserInfo;
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
public class KakaoOAuthService {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USERINFO_URL = "https://kapi.kakao.com/v2/user/me";

    /**
     * Authorization code로 Access Token 받기
     */
    public String getAccessToken(String code, String redirectUri) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("code", code);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                KAKAO_TOKEN_URL,
                request,
                String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();

        } catch (Exception e) {
            log.error("Kakao access token 발급 실패", e);
            throw new ApiException(500, "Kakao 로그인 실패", "액세스 토큰 발급에 실패했습니다.");
        }
    }

    /**
     * Access Token으로 사용자 정보 가져오기
     */
    public KakaoUserInfo getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                KAKAO_USERINFO_URL,
                HttpMethod.GET,
                request,
                String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            KakaoUserInfo userInfo = new KakaoUserInfo();
            userInfo.setId(jsonNode.get("id").asLong());

            JsonNode kakaoAccount = jsonNode.get("kakao_account");
            if (kakaoAccount != null) {
                if (kakaoAccount.has("email")) {
                    userInfo.setEmail(kakaoAccount.get("email").asText());
                }

                JsonNode profile = kakaoAccount.get("profile");
                if (profile != null) {
                    if (profile.has("nickname")) {
                        userInfo.setNickname(profile.get("nickname").asText());
                    }
                    if (profile.has("profile_image_url")) {
                        userInfo.setProfileImage(profile.get("profile_image_url").asText());
                    }
                }
            }

            return userInfo;

        } catch (Exception e) {
            log.error("Kakao 사용자 정보 조회 실패", e);
            throw new ApiException(500, "Kakao 로그인 실패", "사용자 정보 조회에 실패했습니다.");
        }
    }
}

