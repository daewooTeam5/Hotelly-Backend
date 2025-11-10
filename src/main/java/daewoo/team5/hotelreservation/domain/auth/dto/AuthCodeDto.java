package daewoo.team5.hotelreservation.domain.auth.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * OTP 코드 인증 요청 DTO
 * reCAPTCHA 토큰을 포함하여 OTP 무차별 대입 공격을 방지합니다.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthCodeDto {
    /**
     * OTP 인증 코드 (6자리)
     */
    String code;

    /**
     * 사용자 이메일 주소
     */
    String email;

    /**
     * Google reCAPTCHA 검증 토큰
     * OTP 코드 무차별 대입 공격 방지
     */
    String recaptchaToken;
}