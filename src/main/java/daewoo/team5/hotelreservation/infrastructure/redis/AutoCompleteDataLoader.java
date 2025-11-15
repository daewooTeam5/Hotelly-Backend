package daewoo.team5.hotelreservation.infrastructure.redis;

import daewoo.team5.hotelreservation.domain.place.entity.PlacesEntity;
import daewoo.team5.hotelreservation.domain.place.entity.RegionEntity;
import daewoo.team5.hotelreservation.domain.place.repository.PlaceRepository;
import daewoo.team5.hotelreservation.domain.place.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AutoCompleteDataLoader implements CommandLineRunner {

    private final AutoCompleteService redisService;
    private final RegionRepository regionRepository;
    private final PlaceRepository placeRepository;

    @Override
    public void run(String... args) {
        List<RegionEntity> regions = regionRepository.findAll();
        for (RegionEntity r : regions) {
            if (r.getSido() != null) {
                redisService.saveKeyword(r.getSido());
            }
            if (r.getSigungu() != null) {
                redisService.saveKeyword(r.getSigungu());
            }
            if (r.getDong() != null) {
                redisService.saveKeyword(r.getDong());
            }
        }

        // 2) 숙소명 데이터
        List<PlacesEntity> places = placeRepository.findAll();
        for (PlacesEntity p : places) {
            redisService.saveKeyword(p.getName());
        }

        System.out.println("자동완성 초기 데이터 적재 완료 ✅");
    }
}
