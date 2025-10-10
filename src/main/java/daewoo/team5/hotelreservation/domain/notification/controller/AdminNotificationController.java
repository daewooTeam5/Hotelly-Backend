package daewoo.team5.hotelreservation.domain.notification.controller;

import daewoo.team5.hotelreservation.domain.notification.entity.NotificationEntity;
import daewoo.team5.hotelreservation.domain.notification.repository.NotificationRepository;
import daewoo.team5.hotelreservation.infrastructure.firebasefcm.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/admin")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final FcmService fcmService;
    private final NotificationRepository notificationRepository;

    /**
     * 전체 사용자에게 공지 발송 + DB 저장
     */
    @PostMapping("/notice")
    public String sendGlobalNotice(@RequestBody NoticeRequest request) {
        try {
            // 🔹 FCM 전체 발송 ("all" 토픽)
            fcmService.sendToTopic("all", request.getTitle(), request.getBody(), null);

            // 🔹 DB 저장 (user 없이)
            NotificationEntity notification = NotificationEntity.builder()
                    .title(request.getTitle())
                    .content(request.getBody())
                    .notificationType(NotificationEntity.NotificationType.ADMIN)
                    .user(null) // 전체 공지는 특정 유저 없음
                    .build();
            notificationRepository.save(notification);

            return "전체 공지가 발송 및 저장되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            return "공지 발송 실패: " + e.getMessage();
        }
    }

    @lombok.Data
    public static class NoticeRequest {
        private String title;
        private String body;
    }
}
