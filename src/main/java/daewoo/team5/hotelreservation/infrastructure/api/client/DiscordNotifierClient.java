package daewoo.team5.hotelreservation.infrastructure.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name="discordClient",
        url="${DISCORD_WEBHOOK_URL}"
)
public interface DiscordNotifierClient {
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    void sendMessage(@RequestBody Map<String, Object> payload);
}
