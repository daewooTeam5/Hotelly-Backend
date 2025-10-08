package daewoo.team5.hotelreservation.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KakaoLoginRequest {
    private String code;  // Kakao에서 받은 authorization code
    private String redirectUri;  // 프론트엔드의 redirect URI
}

