package daewoo.team5.hotelreservation.domain.users.dto.request;

import com.fasterxml.jackson.core.JsonToken;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDTO {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private String profileImageUrl;

}