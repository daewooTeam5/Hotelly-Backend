package daewoo.team5.hotelreservation.domain.users.dto.request;

import daewoo.team5.hotelreservation.domain.users.entity.UsersEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String userId;
    private String email;
    private String name;
    private String phone;
    private UsersEntity.Role role;
    private UsersEntity.Status status;
    private Long point;


}