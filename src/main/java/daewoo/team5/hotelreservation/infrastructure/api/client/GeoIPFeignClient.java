package daewoo.team5.hotelreservation.infrastructure.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "geoIPClient", url = "https://ipapi.co")
public interface GeoIPFeignClient {
    @GetMapping("/{ip}/json/")
    JsonNode getLocation(@PathVariable("ip") String ip);
}
