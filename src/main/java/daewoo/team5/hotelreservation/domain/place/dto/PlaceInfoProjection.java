package daewoo.team5.hotelreservation.domain.place.dto;

import daewoo.team5.hotelreservation.domain.place.entity.PlacesEntity;

import java.math.BigDecimal;

public interface PlaceInfoProjection {
    Long getPlaceId();
    String getPlaceName();
    String getDescription();
    PlacesEntity.Status getStatus();   // Enum 그대로 받음
    Boolean getIsPublic();
    BigDecimal getAvgRating();
    Integer getReviewCount();
    String getSido();
    String getSigungu();
    String getRoadName();
    String getDetailAddress();
    String getFileUrl();
}
