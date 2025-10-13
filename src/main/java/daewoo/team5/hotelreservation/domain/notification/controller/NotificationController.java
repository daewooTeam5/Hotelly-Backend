package daewoo.team5.hotelreservation.domain.notification.controller;

import daewoo.team5.hotelreservation.domain.notification.entity.NotificationEntity;
import daewoo.team5.hotelreservation.domain.notification.service.NotificationService;
import daewoo.team5.hotelreservation.domain.users.projection.UserProjection;
import daewoo.team5.hotelreservation.global.aop.annotation.AuthUser;
import daewoo.team5.hotelreservation.global.core.common.ApiResult;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/notification")
public class NotificationController {
    private final NotificationService notificationService;

    @AuthUser
    @GetMapping("/my")
    public ApiResult<Page<NotificationEntity>> getMyNotification(
            UserProjection user,
            @RequestParam(name = "page",defaultValue = "1") int page
    ){
        return ApiResult.ok(notificationService.getMyNotification(user.getId(), page-1), "알림 조회 성공");
    }

    @AuthUser
    @GetMapping("/my/count")
    public ApiResult<Long> getMyNotificationRead(UserProjection user){
        return ApiResult.ok(notificationService.countByUserIdAndIsReadFalse(user.getId()), "읽지 않은 알림 개수 조회 성공");
    }

    @AuthUser
    @PostMapping("/my/read")
    public ApiResult<Boolean> readMyNotification(UserProjection user){
        notificationService.readMyNotification(user.getId());
        return ApiResult.ok(true, "알림 읽음 처리 성공");

    }
}
