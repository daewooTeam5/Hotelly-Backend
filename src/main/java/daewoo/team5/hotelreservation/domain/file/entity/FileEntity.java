package daewoo.team5.hotelreservation.domain.file.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import daewoo.team5.hotelreservation.domain.users.entity.UsersEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "File")
@Table(name = "file",
        indexes = {
                @Index(name = "idx_file_domain_fileid_type", columnList = "domain, domain_file_id, filetype")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 파일 ID


    @Column(name = "user_id",    updatable = false)
   private Long userId; // 업로더 ID

    @Column(name = "filename", length = 300, nullable = false)
    private String filename; // 파일명

    @Column(name = "extension", length = 50, nullable = false)
    private String extension; // 확장자

    @Column(name = "filetype", length = 10, nullable = false)
    private String filetype; // 파일 유형 (image, video, document)

    @Column(name = "domain_file_id", nullable = false)
    private Long domainFileId; // 도메인 파일 ID (place, room, chat 등에 해당하는 ID)

    @Column(name = "domain", length = 20, nullable = false)
    private String domain; // 도메인명 (place, room, chat)

    @Column(name = "url", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String url; // 파일 URL

    @OneToOne(mappedBy = "profileImage", fetch = FetchType.LAZY)
    @JsonBackReference
    private UsersEntity user;
}
