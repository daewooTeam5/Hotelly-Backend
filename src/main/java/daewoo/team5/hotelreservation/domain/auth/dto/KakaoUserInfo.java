package daewoo.team5.hotelreservation.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserInfo {
    private Long id;  // Kakao user ID
    private String email;
    private String nickname;
    private String profileImage;
}

