package daewoo.team5.hotelreservation.domain.place.event;


import daewoo.team5.hotelreservation.domain.payment.entity.ReservationEntity;
import daewoo.team5.hotelreservation.domain.place.entity.DailyPlaceReservationEntity;
import daewoo.team5.hotelreservation.domain.place.repository.DailyPlaceReservationRepository;
import daewoo.team5.hotelreservation.domain.place.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReservationEventHandler {
    private final ReservationRepository reservationRepository;
    private final DailyPlaceReservationRepository dailyPlaceReservationRepository;

    @EventListener
    @Transactional
    public void eventReservationCancel(ReservationCancelEvent event) {
        log.info("reservation cancel event: {}", event);
        // 예약 아이디가 존재하고 예약 상태가 대기중일때 취소
        ReservationEntity cancelForReservation = reservationRepository.findById(event.reservationId()).orElse(null);
        if (cancelForReservation == null) {
            return;
        }
        if (cancelForReservation.getStatus() == ReservationEntity.ReservationStatus.pending) {
            cancelForReservation.setStatus(ReservationEntity.ReservationStatus.cancelled);
        }
        // 일일 예약 현황에 숙박일 기준 감소됬던 재고수 다시 증가
        List<DailyPlaceReservationEntity> reservationForPeriod = dailyPlaceReservationRepository.findByRoomIdAndDateBetween(cancelForReservation.getRoom().getId(), cancelForReservation.getResevStart(), cancelForReservation.getResevEnd());
        for (DailyPlaceReservationEntity reservation : reservationForPeriod) {
            reservation.setAvailableRoom(reservation.getAvailableRoom()+1);
        }
    }
}
