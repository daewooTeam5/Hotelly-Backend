package daewoo.team5.hotelreservation.domain.auth.controller;

import daewoo.team5.hotelreservation.domain.auth.controller.swagger.AuthSwagger;
import daewoo.team5.hotelreservation.domain.auth.dto.*;
import daewoo.team5.hotelreservation.domain.auth.service.AuthService;
import daewoo.team5.hotelreservation.domain.auth.service.RecaptchaService;
import daewoo.team5.hotelreservation.domain.users.entity.UsersEntity;
import daewoo.team5.hotelreservation.domain.users.projection.UserProjection;
import daewoo.team5.hotelreservation.global.aop.annotation.AuthUser;
import daewoo.team5.hotelreservation.global.core.common.ApiResult;
import daewoo.team5.hotelreservation.global.core.provider.CookieProvider;
import daewoo.team5.hotelreservation.global.core.provider.JwtProvider;
import daewoo.team5.hotelreservation.global.exception.ApiException;
import daewoo.team5.hotelreservation.infrastructure.firebasefcm.FcmService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthSwagger {
    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final CookieProvider cookieProvider;
    private final FcmService fcmService;
    private final RecaptchaService recaptchaService;

    /**
     * 이메일 로그인 - OTP 코드 전송
     */
    @PostMapping
    public ApiResult<Boolean> emailLogin(@RequestBody @Valid EmailLoginDto emailLoginDto) {
        log.info("Email Login Request Received: {}", emailLoginDto);
        authService.sendOtpCode(emailLoginDto.getEmail());
        return ApiResult.ok(true, "인증 코드가 이메일로 전송되었습니다.");
    }

    @PostMapping("/logout")
    @AuthUser
    public ApiResult<Boolean> logout(
            UserProjection user,
            @RequestBody(required = false) LogoutDto logoutDto,
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        // refreshToken 쿠키 삭제
        cookieProvider.removeCookie("refreshToken", response);
        authService.logout(user, logoutDto, refreshToken);
        return ApiResult.ok(true, "로그아웃 되었습니다.");
    }

    @PostMapping("/code")
    public ApiResult<LoginSuccessDto> authOtpCode(
            @RequestBody @Valid AuthCodeDto authCodeDto,
            HttpServletResponse response
    ) {
        log.info("OTP Code Verification Request: {}", authCodeDto.getEmail());

        // reCAPTCHA 검증
        if (authCodeDto.getRecaptchaToken() == null || authCodeDto.getRecaptchaToken().isEmpty()) {
            log.warn("reCAPTCHA 토큰이 없습니다 - Email: {}", authCodeDto.getEmail());
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "reCAPTCHA 실패", "reCAPTCHA 토큰이 없습니다.");
        }

        boolean isVerified = recaptchaService.verifyToken(authCodeDto.getRecaptchaToken());
        if (!isVerified) {
            log.warn("reCAPTCHA 검증 실패 - Email: {}", authCodeDto.getEmail());
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "reCAPTCHA 실패", "reCAPTCHA 검증에 실패했습니다. 다시 시도해주세요.");
        }

        log.info("reCAPTCHA 검증 성공 - Email: {}", authCodeDto.getEmail());

        UserProjection users = authService.authLogInOtpCode(authCodeDto.getEmail(), authCodeDto.getCode());
        String accessToken = jwtProvider.generateToken(users, JwtProvider.TokenType.ACCESS);
        String refreshToken = jwtProvider.generateToken(users.getId(), JwtProvider.TokenType.REFRESH);
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60);
        response.addCookie(cookie);
        LoginSuccessDto loginSuccessDto = new LoginSuccessDto(accessToken, users);
        return ApiResult.ok(loginSuccessDto, "인증 성공");
    }

    @PostMapping("/token")
    public ApiResult<Map<String, String>> reissueToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED.value(), "토큰 재발급 실패", "리프레시 토큰이 없습니다. 다시 로그인 해주세요.");
        }
        String accessToken = authService.reissueToken(refreshToken, response);
        return ApiResult.ok(Map.of("accessToken", accessToken), "토큰 재발급 성공");
    }

    @AuthUser
    @GetMapping("/test1")
    public ApiResult<UserProjection> test(UserProjection user) {
        return ApiResult.ok(user, "테스트 성공");
    }

    @PostMapping("/signup")
    public ApiResult<UsersEntity> signUp(@RequestBody SignUpRequest signUpRequest) {
        UsersEntity data = authService.adminSignUp(signUpRequest);
        data.setPassword(null);
        return ApiResult.ok(data, "회원가입 성공");
    }

    /**
     * 관리자 로그인
     * reCAPTCHA 검증을 통해 브루트포스 공격 방지
     */
    @PostMapping("/admin/login")
    public ApiResult<LoginSuccessDto> adminLogin(
            @RequestBody @Valid AdminLoginDto adminLoginDto,
            HttpServletResponse response
    ) {
        log.info("Admin Login Request Received: {}", adminLoginDto.getAdminId());

        // reCAPTCHA 검증
        if (adminLoginDto.getRecaptchaToken() == null || adminLoginDto.getRecaptchaToken().isEmpty()) {
            log.warn("reCAPTCHA 토큰이 없습니다 - AdminId: {}", adminLoginDto.getAdminId());
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "reCAPTCHA 실패", "reCAPTCHA 토큰이 없습니다.");
        }

        boolean isVerified = recaptchaService.verifyToken(adminLoginDto.getRecaptchaToken());
        if (!isVerified) {
            log.warn("reCAPTCHA 검증 실패 - AdminId: {}", adminLoginDto.getAdminId());
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "reCAPTCHA 실패", "reCAPTCHA 검증에 실패했습니다. 다시 시도해주세요.");
        }

        log.info("reCAPTCHA 검증 성공 - AdminId: {}", adminLoginDto.getAdminId());
        LoginSuccessDto loginSuccessDto = authService.adminLogin(adminLoginDto, response);
        return ApiResult.ok(loginSuccessDto, "관리자 로그인 성공");
    }

    @PostMapping("/fcm-token")
    @AuthUser
    public ApiResult<Boolean> saveFcmToken(
            UserProjection user,
            @RequestBody SaveFcmTokenDto dto
    ) {
        String firebaseToken = authService.saveFcmToken(user.getId(), dto.getFcmToken(), dto.getDevice());
        fcmService.subscribeToTopic("all", firebaseToken);
        return ApiResult.ok(true, "FCM 토큰 저장 성공");
    }

    @PostMapping("/google")
    public ApiResult<LoginSuccessDto> googleLogin(
            @RequestBody GoogleLoginRequest request,
            HttpServletResponse response
    ) {
        LoginSuccessDto loginSuccessDto = authService.googleLogin(request.getCode(), request.getRedirectUri(), response);
        return ApiResult.ok(loginSuccessDto, "Google 로그인 성공");
    }

    @PostMapping("/kakao")
    public ApiResult<LoginSuccessDto> kakaoLogin(
            @RequestBody KakaoLoginRequest request,
            HttpServletResponse response
    ) {
        LoginSuccessDto loginSuccessDto = authService.kakaoLogin(request.getCode(), request.getRedirectUri(), response);
        return ApiResult.ok(loginSuccessDto, "Kakao 로그인 성공");
    }
}