package daewoo.team5.hotelreservation.domain.auth.entity;

import daewoo.team5.hotelreservation.domain.users.entity.UsersEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "userFcm")
@Table(name = "user_fcm")
public class UserFcmEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UsersEntity user;

    private String token;

    private Boolean isSubscribed;

    @Column
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    public enum DeviceType {
        WEB,
        ANDROID,
        IOS
    }
}
