package daewoo.team5.hotelreservation.domain.users.projection;


import java.time.LocalDateTime;

public interface MyInfoProjection {
    String getName();
    String getEmail();
    String getUserId();
    Long getId();
    String getRole();
    String getPhone();
    String getStatus();
    LocalDateTime createdAt();
    Long getPoint();
    String getProfileImageUrl();


}
