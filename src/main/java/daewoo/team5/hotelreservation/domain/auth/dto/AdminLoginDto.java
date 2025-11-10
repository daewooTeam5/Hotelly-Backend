package daewoo.team5.hotelreservation.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 관리자 로그인 요청 DTO
 * reCAPTCHA 토큰을 포함하여 관리자 계정에 대한 브루트포스 공격을 방지합니다.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginDto {
    /**
     * 관리자 아이디
     */
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String adminId;

    /**
     * 관리자 비밀번호
     */
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String adminPassword;

    /**
     * Google reCAPTCHA 검증 토큰
     * 프론트엔드에서 reCAPTCHA 위젯을 통해 받은 토큰
     */
    private String recaptchaToken;
}