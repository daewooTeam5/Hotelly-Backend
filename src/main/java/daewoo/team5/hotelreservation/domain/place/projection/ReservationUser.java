package daewoo.team5.hotelreservation.domain.place.projection;

import java.time.LocalDate;

public interface ReservationUser {
    Long getUserId();
    String getUserName();
    String getUserEmail();
    Long getReservationId();
    String getUserFcmToken();
    String getHotelName();
    String getRoomName();
    LocalDate getResevStart();
    LocalDate getResevEnd();

}
