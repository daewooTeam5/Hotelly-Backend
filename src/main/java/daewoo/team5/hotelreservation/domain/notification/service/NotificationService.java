package daewoo.team5.hotelreservation.domain.notification.service;

import daewoo.team5.hotelreservation.domain.auth.entity.UserFcmEntity;
import daewoo.team5.hotelreservation.domain.auth.repository.UserFcmRepository;
import daewoo.team5.hotelreservation.domain.notification.entity.NotificationEntity;
import daewoo.team5.hotelreservation.domain.notification.entity.NotificationReadEntity;
import daewoo.team5.hotelreservation.domain.notification.repository.NotificationReadRepository;
import daewoo.team5.hotelreservation.domain.notification.repository.NotificationRepository;
import daewoo.team5.hotelreservation.domain.users.repository.UsersRepository;
import daewoo.team5.hotelreservation.global.exception.ApiException;
import daewoo.team5.hotelreservation.global.exception.UserNotFoundException;
import daewoo.team5.hotelreservation.infrastructure.firebasefcm.FcmService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final UsersRepository usersRepository;
    private final FcmService fcmService;
    private final UserFcmRepository userFcmRepository;


    @Transactional
    public void sendNotification(String title, String content, String link, String toToken, NotificationEntity.NotificationType type) {
        try{
            UserFcmEntity receivedUser = userFcmRepository.findByToken(toToken).orElseThrow(()-> new ApiException(HttpStatus.NOT_FOUND,"유저 토큰이 존재하지않습니다.","FCM 전송 실패"));
            // 유저가 알림 수신거부할시 FCM 알림 전송 안함
            notificationRepository.save(
                    NotificationEntity
                            .builder()
                            .content(content)
                            .title(title)
                            .user(receivedUser.getUser())
                            .link(link)
                            .notificationType(type)
                            .build()
            );
            if(!receivedUser.getIsSubscribed()){
                return;
            }
            fcmService.sendToToken(toToken,title,content,link);
        }catch (Exception e){
            throw new IllegalStateException("FCM 전송 실패");
        }

    }

    public Page<NotificationEntity> getMyNotification(Long userId, int page) {
        return notificationRepository.findByUserIdOrUserIdIsNullOrderByCreatedAtDesc(userId, PageRequest.of(page, 10));
    }

    public Long countByUserIdAndIsReadFalse(Long userId) {
        Optional<NotificationReadEntity> userNotiRead = notificationReadRepository.findByUserId(userId);
        if (userNotiRead.isEmpty()) {

            return notificationRepository.countByUserId(userId);
        }

        return notificationReadRepository.findByUserIdReadCountWithNow(userId);
    }

    public void readMyNotification(Long userId) {
        notificationReadRepository.findByUserId(userId).ifPresentOrElse(
                notiRead -> {
                    notiRead.setReadAt(LocalDateTime.now());
                    notificationReadRepository.save(notiRead);
                },
                () -> {
                    NotificationReadEntity newNotiRead = NotificationReadEntity.builder()
                            .user(usersRepository.findById(userId).orElseThrow(UserNotFoundException::new))
                            .readAt(LocalDateTime.now())
                            .build();
                    notificationReadRepository.save(newNotiRead);
                }
        );

    }
}
