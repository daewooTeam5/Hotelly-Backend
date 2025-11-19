package daewoo.team5.hotelreservation.domain.users.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import daewoo.team5.hotelreservation.domain.file.entity.FileEntity;
import jakarta.persistence.*;
import lombok.*;
import daewoo.team5.hotelreservation.global.core.common.BaseTimeEntity;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity(name = "Users")
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsersEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(length = 100)
    private String email;

    @Column(nullable = true, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('customer', 'hotel_owner', 'admin', 'place_admin', 'user_admin') DEFAULT 'customer'")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('active', 'inactive', 'banned', 'withdraw') DEFAULT 'active'")
    private Status status;


    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('google','email','kakao','admin')")
    private UserType userType;


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "file_id")
    @JsonManagedReference
    private FileEntity profileImage;


    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = true ,columnDefinition = "BIGINT DEFAULT 0")
    @Builder.Default
    private Long point=0L;

    public void updateProfile(String name, String email, String phone) {

        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (email != null && !email.isBlank()) {
            this.email = email;
        }
        if (phone != null && !phone.isBlank()) {
            this.phone = phone;
        }
    }

    public enum Role {
        customer,
        hotel_owner,
        admin,
        place_admin,
        user_admin
    }

    public enum Status {
        active,
        inactive,
        banned,
        withdraw
    }

    public enum UserType{
        google,
        email,
        kakao,
        admin
    }
}

