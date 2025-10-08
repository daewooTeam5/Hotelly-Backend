package daewoo.team5.hotelreservation.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserInfo {
    private String sub;  // Google user ID
    private String email;
    private String name;
    private String picture;
    private Boolean emailVerified;
}

