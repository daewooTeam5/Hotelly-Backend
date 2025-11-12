package daewoo.team5.hotelreservation.domain.place.review.service;

import daewoo.team5.hotelreservation.domain.place.review.dto.CreateReviewCommentRequest;
import daewoo.team5.hotelreservation.domain.place.review.entity.Review;
import daewoo.team5.hotelreservation.domain.place.review.entity.ReviewComment;
import daewoo.team5.hotelreservation.domain.place.review.projection.ReviewCommentProjection;
import daewoo.team5.hotelreservation.domain.place.review.repository.ReviewCommentRepository;
import daewoo.team5.hotelreservation.domain.place.review.repository.ReviewRepository;
import daewoo.team5.hotelreservation.domain.users.entity.UsersEntity;
import daewoo.team5.hotelreservation.domain.users.projection.UserProjection;
import daewoo.team5.hotelreservation.domain.users.repository.UsersRepository;
import daewoo.team5.hotelreservation.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommentService {

    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final UsersRepository usersRepository;

    // ✅ 기존 메서드: 댓글 생성 또는 수정
    public void createComment(Long reviewId, CreateReviewCommentRequest request, UserProjection userProjection) {
        Review review = reviewRepository.findByIdWithPlaceAndOwner(reviewId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다.", "존재하지 않는 리뷰입니다."));

        UsersEntity manager = usersRepository.findById(userProjection.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.", "존재하지 않는 사용자입니다."));

        if (!review.getPlace().getOwner().getId().equals(manager.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "권한 없음", "숙소 관리자만 댓글을 작성할 수 있습니다.");
        }

        // ✅ 이미 댓글이 있으면 수정, 없으면 생성
        if (review.getCommentByOwner() != null) {
            // 수정
            ReviewComment existingComment = review.getCommentByOwner();
            existingComment.updateComment(request.getComment());
            reviewCommentRepository.save(existingComment);
        } else {
            // 생성
            ReviewComment comment = ReviewComment.builder()
                    .comment(request.getComment())
                    .review(review)
                    .user(manager)
                    .build();
            reviewCommentRepository.save(comment);
        }
    }

    public List<ReviewCommentProjection> getUserReviewComments(Long userId) {
        return reviewCommentRepository.findReviewCommentsByUserId(userId);
    }
}