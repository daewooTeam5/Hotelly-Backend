package daewoo.team5.hotelreservation.domain.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import daewoo.team5.hotelreservation.domain.auth.dto.AdminLoginDto;
import daewoo.team5.hotelreservation.domain.auth.dto.GoogleUserInfo;
import daewoo.team5.hotelreservation.domain.auth.dto.KakaoUserInfo;
import daewoo.team5.hotelreservation.domain.auth.dto.LoginSuccessDto;
import daewoo.team5.hotelreservation.domain.auth.dto.SignUpRequest;
import daewoo.team5.hotelreservation.domain.auth.entity.UserFcmEntity;
import daewoo.team5.hotelreservation.domain.auth.repository.BlackListRepository;
import daewoo.team5.hotelreservation.domain.auth.repository.FcmCacheRepository;
import daewoo.team5.hotelreservation.domain.auth.repository.OtpRepository;
import daewoo.team5.hotelreservation.domain.auth.repository.UserFcmRepository;
import daewoo.team5.hotelreservation.domain.file.service.FileService;
import daewoo.team5.hotelreservation.domain.file.entity.FileEntity;
import daewoo.team5.hotelreservation.domain.users.entity.Users;
import daewoo.team5.hotelreservation.domain.users.projection.UserProjection;
import daewoo.team5.hotelreservation.domain.users.repository.UsersRepository;
import daewoo.team5.hotelreservation.global.core.provider.CookieProvider;
import daewoo.team5.hotelreservation.global.core.provider.JwtProvider;
import daewoo.team5.hotelreservation.global.exception.ApiException;
import daewoo.team5.hotelreservation.global.exception.UserNotFoundException;
import daewoo.team5.hotelreservation.global.mail.service.MailService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final OtpRepository otpRepository;
    private final MailService mailService;
    private final UsersRepository userRepository;
    private final BlackListRepository blackListRepository;
    private final CookieProvider cookieProvider;
    private final JwtProvider jwtProvider;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final FcmCacheRepository fcmCacheRepository;
    private final UserFcmRepository userFcmRepository;
    private final GoogleOAuthService googleOAuthService;
    private final KakaoOAuthService kakaoOAuthService;
    private final FileService fileService;

    // null 이면 비회원, null 아니면 회원
    public UserProjection isAuthUser(Authentication auth) {
        UserProjection currentUser = null;
        if (auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)) {
            Object principal = auth.getPrincipal();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = null;
            try {
                node = mapper.readTree(principal.toString());
            } catch (JsonProcessingException e) {
                throw new ApiException(500, "서버 오류", "서버 오류가 발생했습니다. 개발자에게 문의해주세요");
            }
            currentUser = usersRepository.findById(Long.parseLong(node.toString()), UserProjection.class)
                    .orElseThrow(() -> new ApiException(404, "존재하지 않는 유저", "존재 하지 않는 유저입니다."));
        }
        return currentUser;
    }

    public void logout(String refreshToken) {
        long expirationTime = jwtProvider.parseClaims(refreshToken).getExpiration().getTime();
        blackListRepository.addToBlackList(refreshToken, expirationTime);
    }

    public Users adminSignUp(SignUpRequest signUpRequest) {
        signUpRequest.setAdminPassword(passwordEncoder.encode(signUpRequest.getAdminPassword()));

        Users.Role role;
        try {
            role = Users.Role.valueOf(signUpRequest.getAdminRole());
        } catch (IllegalArgumentException e) {
            role = Users.Role.admin;
        }

        return userRepository.save(Users.builder()
                .email(signUpRequest.getAdminId() + "@daewoo.ac.kr")
                .name(signUpRequest.getAdminName())
                .userId(signUpRequest.getAdminId())
                .password(signUpRequest.getAdminPassword())
                .role(role)
                .userType(Users.UserType.admin)
                .status(Users.Status.inactive)
                .build());
    }

    public LoginSuccessDto adminLogin(AdminLoginDto adminLoginDto, HttpServletResponse response) {
        Users admin = userRepository.findByUserId(adminLoginDto.getAdminId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자 없음", "해당 관리자가 존재하지 않습니다."));
        if (!passwordEncoder.matches(adminLoginDto.getAdminPassword(), admin.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "로그인 실패", "비밀번호가 일치하지 않습니다.");
        }
        UserProjection projection = userRepository.findById(admin.getId(), UserProjection.class)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "사용자 없음", "해당 관리자가 존재하지 않습니다."));

        String accessToken = jwtProvider.generateToken(projection, JwtProvider.TokenType.ACCESS);
        String refreshToken = jwtProvider.generateToken(projection.getId(), JwtProvider.TokenType.REFRESH);

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30일
        response.addCookie(cookie);

        return new LoginSuccessDto(accessToken, projection);
    }

    public void sendOtpCode(String email) {
        String optCode = otpRepository.generateOtp(email);
        log.info("Generated OTP Code: {}", optCode);
        mailService.sendOtpCode(email, optCode);
    }

    @Transactional
    public UserProjection authLogInOtpCode(String email, String code) {
        boolean isValid = otpRepository.validateOtp(email, code);
        if (!isValid) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "인증 실패", "유효하지 않은 인증 코드입니다.");
        }
        Optional<Users> findUser = userRepository.findByEmailAndUserType(email, Users.UserType.email);
        Random random = new Random();
        Users users = findUser.orElseGet(() -> userRepository.save(
                Users.builder()
                        .email(email)
                        .point(0L)
                        .name("Guest" + random.nextInt())
                        .userId(UUID.randomUUID().toString())
                        .role(Users.Role.customer)
                        .status(Users.Status.active)
                        .userType(Users.UserType.email)
                        .build()

        ));
        return userRepository.findByName(users.getName(), UserProjection.class).get();

    }

    public String reissueToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || !jwtProvider.validateToken(refreshToken)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "토큰 재발급 실패", "유효하지 않은 리프레시 토큰입니다.");
        }
        if (blackListRepository.isBlackListed(refreshToken)) {
            cookieProvider.removeCookie("refreshToken", response);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "토큰 재발급 실패", "예기치 못한 오류 발생");
        }
        Claims tokenParse = jwtProvider.parseClaims(refreshToken);
        tokenParse.getSubject();
        Long userId = Long.parseLong(tokenParse.getSubject());
        UserProjection users = userRepository.findById(userId, UserProjection.class).orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "사용자 없음", "해당 사용자가 존재하지 않습니다.")
        );
        String newAccessToken = jwtProvider.generateToken(users, JwtProvider.TokenType.ACCESS);
        String newRefreshToken = jwtProvider.generateToken(users.getId(), tokenParse.getExpiration().getTime());
        Cookie refreshTokenCookie = new Cookie("refreshToken", newRefreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) ((tokenParse.getExpiration().getTime() - System.currentTimeMillis()) / 1000));
        response.addCookie(refreshTokenCookie);
        return newAccessToken;
    }

    public String saveFcmToken(Long userId, String fcmToken) {
        String cacheFcmToken = fcmCacheRepository.getFcmToken(userId);

        // 1. 캐시에 토큰 존재하고 같으면 그대로 리턴
        if (cacheFcmToken != null && cacheFcmToken.equals(fcmToken)) {
            return cacheFcmToken;
        }

        // 2. DB에서 토큰 조회
        Optional<UserFcmEntity> users = userFcmRepository.findByUserId(userId);
        if (users.isPresent()) {
            UserFcmEntity userFcmEntity = users.get();
            // 2-1. DB에 토큰이 존재하고, 다를 경우 -> 업데이트
            if (!userFcmEntity.getToken().equals(fcmToken)) {
                userFcmEntity.setToken(fcmToken);
                userFcmRepository.save(userFcmEntity);
                fcmCacheRepository.saveFcmToken(userId, fcmToken);
            }
        } else {
            // 2-2. DB에 토큰이 존재하지 않을 경우 -> 생성
            userFcmRepository.save(UserFcmEntity.builder()
                    .user(usersRepository.findById(userId).orElseThrow(UserNotFoundException::new))
                    .token(fcmToken)
                    .build());
            fcmCacheRepository.saveFcmToken(userId, fcmToken);
        }
        return fcmToken;
    }

    @Transactional
    public LoginSuccessDto googleLogin(String code, String redirectUri, HttpServletResponse response) {
        // 1. Authorization code로 Access Token 받기
        String accessToken = googleOAuthService.getAccessToken(code, redirectUri);

        // 2. Access Token으로 사용자 정보 가져오기
        GoogleUserInfo googleUserInfo = googleOAuthService.getUserInfo(accessToken);

        // 3. DB에서 사용자 조회 또는 생성
        Optional<Users> findUser = userRepository.findByEmailAndUserType(googleUserInfo.getEmail(), Users.UserType.google);


        Users user = findUser.orElseGet(() -> {
            // 신규 사용자 생성
            Users newUser = Users.builder()
                    .email(googleUserInfo.getEmail())
                    .name(googleUserInfo.getName())
                    .userId("google_" + googleUserInfo.getSub())
                    .role(Users.Role.customer)
                    .status(Users.Status.active)
                    .userType(Users.UserType.google)
                    .point(0L)
                    .build();
            Users saveUser = userRepository.save(newUser);
            if (googleUserInfo.getPicture() != null) {
                FileEntity profileImage = fileService.save(googleUserInfo.getPicture(), saveUser.getId(), saveUser.getId(), "profile");
                saveUser.setProfileImage(profileImage);
            }
            return saveUser;
        });
        log.info("Google User Info: {}", googleUserInfo.getPicture());

        // 4. JWT 토큰 생성
        UserProjection userProjection = userRepository.findById(user.getId(), UserProjection.class)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자 없음", "해당 사용자가 존재하지 않습니다."));

        String jwtAccessToken = jwtProvider.generateToken(userProjection, JwtProvider.TokenType.ACCESS);
        String refreshToken = jwtProvider.generateToken(userProjection.getId(), JwtProvider.TokenType.REFRESH);

        // 5. Refresh Token을 쿠키에 저장
        ResponseCookie refreshCookie = cookieProvider.generateRefreshTokenCookie(refreshToken);
        response.addHeader("Set-Cookie", refreshCookie.toString());
        return new LoginSuccessDto(jwtAccessToken, userProjection);
    }

    @Transactional
    public LoginSuccessDto kakaoLogin(String code, String redirectUri, HttpServletResponse response) {
        // 1. Authorization code로 Access Token 받기
        String accessToken = kakaoOAuthService.getAccessToken(code, redirectUri);

        // 2. Access Token으로 사용자 정보 가져오기
        KakaoUserInfo kakaoUserInfo = kakaoOAuthService.getUserInfo(accessToken);
        log.info("kakao: "+kakaoUserInfo.toString());

        // 3. DB에서 사용자 조회 또는 생성
        Optional<Users> findUser = userRepository.findByUserIdAndUserType(
            "kakao_" + kakaoUserInfo.getId(),
            Users.UserType.kakao
        );

        Users user = findUser.orElseGet(() -> {
            // 신규 사용자 생성
            Users newUser = Users.builder()
                    .email(kakaoUserInfo.getEmail())
                    .name(kakaoUserInfo.getNickname() != null ? kakaoUserInfo.getNickname() : "Kakao User")
                    .userId("kakao_" + kakaoUserInfo.getId())
                    .role(Users.Role.customer)
                    .status(Users.Status.active)
                    .userType(Users.UserType.kakao)
                    .point(0L)
                    .build();
            Users saveUser = userRepository.save(newUser);
            if (kakaoUserInfo.getProfileImage() != null) {
                FileEntity profileImage = fileService.save(kakaoUserInfo.getProfileImage(), saveUser.getId(), saveUser.getId(), "profile");
                saveUser.setProfileImage(profileImage);
            }
            return saveUser;
        });
        log.info("Kakao User Info: {}", kakaoUserInfo.getProfileImage());

        // 4. JWT 토큰 생성
        UserProjection userProjection = userRepository.findById(user.getId(), UserProjection.class)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자 없음", "해당 사용자가 존재하지 않습니다."));

        String jwtAccessToken = jwtProvider.generateToken(userProjection, JwtProvider.TokenType.ACCESS);
        String refreshToken = jwtProvider.generateToken(userProjection.getId(), JwtProvider.TokenType.REFRESH);

        // 5. Refresh Token을 쿠키에 저장
        ResponseCookie refreshCookie = cookieProvider.generateRefreshTokenCookie(refreshToken);
        response.addHeader("Set-Cookie", refreshCookie.toString());
        return new LoginSuccessDto(jwtAccessToken, userProjection);
    }
}
