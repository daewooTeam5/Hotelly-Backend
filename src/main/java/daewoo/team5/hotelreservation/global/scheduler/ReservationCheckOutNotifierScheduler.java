package daewoo.team5.hotelreservation.global.scheduler;


import daewoo.team5.hotelreservation.domain.place.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationCheckOutNotifierScheduler {

    private final ReservationService reservationService;

    @Scheduled(cron = "0 0 18 * * ?")
    public void notifier(){
        reservationService.checkOutNotifier(LocalDate.now());
    }
}
