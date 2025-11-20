package daewoo.team5.hotelreservation.infrastructure.webhook;


import com.fasterxml.jackson.databind.JsonNode;
import daewoo.team5.hotelreservation.infrastructure.api.client.DiscordNotifierClient;
import daewoo.team5.hotelreservation.infrastructure.api.client.GeoIpCacheClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ua_parser.Client;
import ua_parser.Parser;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordNotifier {

    private final DiscordNotifierClient discordFeignClient;
    private final GeoIpCacheClient geoIpCacheClient;
    private final Parser uaParser = new Parser();

    @Async
    public void sendError(String title, Map<String, String> fields) {
        try {
            String ip = fields.get("IP 주소");
            String location = getLocationFromIP(ip);
            fields.put("추정 위치", location);

            StringBuilder desc = new StringBuilder();
            fields.forEach((k, v) -> desc.append("**").append(k).append(":** ").append(v).append("\n"));

            Map<String, Object> embed = new HashMap<>();
            embed.put("title", title);
            embed.put("description", desc.toString());
            embed.put("color", 15158332);

            Map<String, Object> payload = Map.of(
                    "embeds", new Object[]{embed}
            );

            discordFeignClient.sendMessage(payload);
        } catch (Exception e) {
            log.error("⚠️ Discord Webhook 전송 실패", e);
        }
    }

    private String getLocationFromIP(String ip) {
        if (ip == null || ip.equals("0:0:0:0:0:0:0:1")) {
            return "로컬호스트 (127.0.0.1)";
        }

        try {
            JsonNode json = geoIpCacheClient.getGeoLocation(ip);

            String city = json.path("city").asText("");
            String region = json.path("region").asText("");
            String country = json.path("country_name").asText("");
            String org = json.path("org").asText("알 수 없음");
            String latitude = json.path("latitude").asText("?");
            String longitude = json.path("longitude").asText("?");

            StringBuilder sb = new StringBuilder();

            if (!country.isEmpty()) sb.append(country);
            if (!region.isEmpty()) sb.append(" ").append(region);
            if (!city.isEmpty()) sb.append(" ").append(city);

            sb.append(String.format(" (%.5s, %.5s)", latitude, longitude)); // 위도, 경도
            sb.append(" / ").append(org); // 통신사

            return sb.toString().trim();

        } catch (Exception e) {
            log.warn("GeoIP 조회 실패: {}", e.getMessage());
            return "위치 조회 실패";
        }
    }

    public String parseDeviceInfo(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) return "정보 없음";
        try {
            Client client = uaParser.parse(userAgent);
            return String.format("%s / %s / %s",
                    client.device.family,
                    client.os.family,
                    client.userAgent.family);
        } catch (Exception e) {
            log.warn("User-Agent 파싱 실패: {}", e.getMessage());
            return userAgent;
        }
    }
}
