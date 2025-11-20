package daewoo.team5.hotelreservation.global.aop.aspect;

import daewoo.team5.hotelreservation.domain.users.projection.UserProjection;
import daewoo.team5.hotelreservation.domain.users.repository.UsersRepository;
import daewoo.team5.hotelreservation.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthUserAspect {
    private static final Logger log = LoggerFactory.getLogger(AuthUserAspect.class);
    private final UsersRepository usersRepository;

    @Around("@annotation(daewoo.team5.hotelreservation.global.aop.annotation.AuthUser)")
    public Object injectCurrentUser(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return joinPoint.proceed(args);

        log.info("aaaa {}", auth);
        log.info("aaaa {}", auth.getPrincipal());
        Object principal = auth.getPrincipal();
        UserProjection currentUser = usersRepository.findById(Long.parseLong(principal.toString()), UserProjection.class)
                .orElseThrow(() -> new ApiException(404, "존재하지 않는 유저", "존재 하지 않는 유저입니다."));

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?>[] paramTypes = signature.getMethod().getParameterTypes();
        for (int i = 0; i < args.length; i++) {
            if (UserProjection.class.isAssignableFrom(paramTypes[i])) {
                args[i] = currentUser;
            }
        }
        return joinPoint.proceed(args);
    }
}
