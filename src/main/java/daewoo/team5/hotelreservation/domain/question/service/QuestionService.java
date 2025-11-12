package daewoo.team5.hotelreservation.domain.question.service;

import daewoo.team5.hotelreservation.domain.place.entity.PlacesEntity;
import daewoo.team5.hotelreservation.domain.place.repository.PlaceRepository;
import daewoo.team5.hotelreservation.domain.question.dto.*;
import daewoo.team5.hotelreservation.domain.question.entity.QuestionEntity;
import daewoo.team5.hotelreservation.domain.question.projection.QuestionProjection;
import daewoo.team5.hotelreservation.domain.question.repository.QuestionRepository;
import daewoo.team5.hotelreservation.domain.users.entity.UsersEntity;
import daewoo.team5.hotelreservation.domain.users.projection.UserProjection;
import daewoo.team5.hotelreservation.domain.users.repository.UsersRepository;
import daewoo.team5.hotelreservation.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import daewoo.team5.hotelreservation.domain.question.specification.QuestionSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final PlaceRepository placeRepository;
    private final UsersRepository usersRepository;

    // 문의 등록
    public QuestionResponse createQuestion(Long placeId, CreateQuestionRequest request, UserProjection userProjection) {
        UsersEntity user = usersRepository.findById(userProjection.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.", "존재하지 않는 사용자입니다."));

        PlacesEntity place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "숙소를 찾을 수 없습니다.", "존재하지 않는 숙소입니다."));

        QuestionEntity question = QuestionEntity.builder()
                .place(place)
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        questionRepository.save(question);
        return new QuestionResponse(question);
    }

    // 숙소별 문의 목록 조회
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByPlace(Long placeId) {
        return questionRepository.findByPlaceId(placeId).stream()
                .map(QuestionResponse::new)
                .collect(Collectors.toList());
    }

    // 문의 답변 등록 (숙소 주인)
    public void addAnswer(Long questionId, CreateAnswerRequest request, UserProjection userProjection) {
        UsersEntity owner = usersRepository.findById(userProjection.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.", "존재하지 않는 사용자입니다."));

        QuestionEntity question = questionRepository.findByIdWithOwner(questionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "문의를 찾을 수 없습니다.", "존재하지 않는 문의입니다."));

        if (!question.getPlace().getOwner().getId().equals(owner.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "권한 없음", "숙소 관리자만 답변을 작성할 수 있습니다.");
        }

        question.setAnswer(request.getAnswer());
    }
    // 문의 삭제 (숙소 주인)
    public void deleteQuestion(Long questionId, UserProjection userProjection) {
        UsersEntity owner = usersRepository.findById(userProjection.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.", "존재하지 않는 사용자입니다."));

        QuestionEntity question = questionRepository.findByIdWithOwner(questionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "문의를 찾을 수 없습니다.", "존재하지 않는 문의입니다."));

        if (!question.getPlace().getOwner().getId().equals(owner.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "권한 없음", "숙소 관리자만 문의를 삭제할 수 있습니다.");
        }

        questionRepository.delete(question);
    }
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByPlace(Long placeId, UserProjection userProjection) {
        if (userProjection != null) {
            return questionRepository.findByPlaceIdOrderByUserAndCreatedAtDesc(placeId, userProjection.getId()).stream()
                    .map(QuestionResponse::new)
                    .collect(Collectors.toList());
        }
        return questionRepository.findByPlaceId(placeId).stream()
                .map(QuestionResponse::new)
                .collect(Collectors.toList());
    }
    // 관리자 문의 검색
    @Transactional(readOnly = true)
    public List<QuestionResponse> searchQuestions(Long placeId, QuestionSearchRequest searchRequest, UserProjection userProjection) { // UserProjection 파라미터 추가
        // 1. 숙소 정보 조회 및 소유자 확인
        PlacesEntity place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "숙소를 찾을 수 없습니다.", "존재하지 않는 숙소입니다."));
    
        if (!place.getOwner().getId().equals(userProjection.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "권한 없음", "해당 숙소에 대한 조회 권한이 없습니다.");
        }

    // 2. 기존 검색 로직 수행
    Specification<QuestionEntity> spec = QuestionSpecification.filterBy(placeId, searchRequest);
    return questionRepository.findAll(spec).stream()
            .map(QuestionResponse::new)
            .collect(Collectors.toList());
}

    public List<QuestionProjection> getUserQuestions(Long userId) {
        return questionRepository.findQuestionsByUserId(userId);
    }

    public Page<MyQuestionResponse> getMyQuestions(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionEntity> questions = questionRepository.findByUserIdWithDetails(userId, pageable);

        return questions.map(this::convertToMyQuestionResponse);
    }

    private MyQuestionResponse convertToMyQuestionResponse(QuestionEntity question) {
        return MyQuestionResponse.builder()
                .questionId(question.getId())
                .place(MyQuestionResponse.PlaceInfo.builder()
                        .placeId(question.getPlace().getId())
                        .placeName(question.getPlace().getName())
                        .categoryName(question.getPlace().getCategory().getName())
                        .build())
                .title(question.getTitle())
                .content(question.getContent())
                .answer(convertToAnswerInfo(question))
                .createdAt(question.getCreatedAt())
                .build();
    }

    private MyQuestionResponse.AnswerInfo convertToAnswerInfo(QuestionEntity question) {
        if (question.getAnswer() == null || question.getAnswer().isEmpty()) {
            return null;
        }

        return MyQuestionResponse.AnswerInfo.builder()
                .content(question.getAnswer())
                .build();
    }
}
