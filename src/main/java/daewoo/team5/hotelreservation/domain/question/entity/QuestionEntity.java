package daewoo.team5.hotelreservation.domain.question.entity;

import daewoo.team5.hotelreservation.domain.place.entity.PlacesEntity;
import daewoo.team5.hotelreservation.domain.users.entity.UsersEntity;
import daewoo.team5.hotelreservation.global.core.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "Question")
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private PlacesEntity place; // 문의가 달린 숙소

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UsersEntity user; // 문의를 작성한 사용자

    @Column(nullable = false, length = 200)
    private String title; // 문의 제목

    @Lob
    private String content; // 문의 내용

    @Lob
    private String answer; // 호텔 관리자의 답변

    @Builder
    public QuestionEntity(PlacesEntity place, UsersEntity user, String title, String content) {
        this.place = place;
        this.user = user;
        this.title = title;
        this.content = content;
    }

}