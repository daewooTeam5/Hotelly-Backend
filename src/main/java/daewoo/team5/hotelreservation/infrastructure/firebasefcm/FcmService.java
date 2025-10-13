package daewoo.team5.hotelreservation.infrastructure.firebasefcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmService {
    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = { MESSAGING_SCOPE };

    private String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new FileInputStream("config/firebase-service.json"))
                .createScoped(Arrays.asList(SCOPES));
        googleCredentials.refresh();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    // ✅ 개별 토큰으로 메시지 보내기 (notification → data 기반)
    public String sendToToken(String token, String title, String body, String link) throws Exception {
        Message.Builder builder = Message.builder()
                .setToken(token)
                .putData("title", title)
                .putData("body", body)
                .putData("click_action", link != null && !link.isEmpty() ? link : "/");

        // 웹 푸시 설정 (필요 시 header 추가 가능)
        builder.setWebpushConfig(WebpushConfig.builder()
                .putHeader("TTL", "86400") // 알림 유효 시간 (1일)
                .build());

        return FirebaseMessaging.getInstance().send(builder.build());
    }

    // ✅ 토픽으로 메시지 보내기 (notification → data 기반)
    public String sendToTopic(String topic, String title, String body, String link) throws Exception {
        Message.Builder builder = Message.builder()
                .setTopic(topic)
                .putData("title", title)
                .putData("body", body)
                .putData("click_action", link != null && !link.isEmpty() ? link : "/");

        builder.setWebpushConfig(WebpushConfig.builder()
                .putHeader("TTL", "86400")
                .build());

        return FirebaseMessaging.getInstance().send(builder.build());
    }

    // ✅ 토픽 구독
    public String subscribeToTopic(String topic, String token) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(List.of(token), topic);
            return "Subscribed successfully to topic: " + topic;
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return "Error subscribing to topic: " + e.getMessage();
        }
    }
}
