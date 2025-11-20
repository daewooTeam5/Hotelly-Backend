package daewoo.team5.hotelreservation.infrastructure.api.client;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeoIpCacheClient {
    private final GeoIPFeignClient geoIPFeignClient;

    @Cacheable(value = "geoip-cache", key = "#ip", unless = "#result == null")
    public JsonNode getGeoLocation(String ip) {
        return geoIPFeignClient.getLocation(ip);
    }
}
