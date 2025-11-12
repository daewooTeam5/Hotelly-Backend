package daewoo.team5.hotelreservation.domain.users.dto.request;

import daewoo.team5.hotelreservation.domain.users.entity.OwnerRequestEntity;
import daewoo.team5.hotelreservation.domain.users.entity.UsersEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class OwnerRequestDto {
    private Long id;
    private Long ownerRequestId;
    private String userId;
    private String email;
    private String name;
    private String phone;
    private UsersEntity.Role role;
    private UsersEntity.Status status;

    private OwnerRequestEntity.Status ownerRequestStatus;
    private String rejectionReason;
    private String businessNumber;

    private List<String> ownerRequestFiles;
}
