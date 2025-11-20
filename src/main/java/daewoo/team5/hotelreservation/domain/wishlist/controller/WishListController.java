package daewoo.team5.hotelreservation.domain.wishlist.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import daewoo.team5.hotelreservation.domain.users.projection.UserProjection;
import daewoo.team5.hotelreservation.domain.wishlist.repository.WishListRepository;
import daewoo.team5.hotelreservation.domain.wishlist.service.WishListService;
import daewoo.team5.hotelreservation.global.aop.annotation.AuthUser;
import daewoo.team5.hotelreservation.global.core.common.ApiResult;
import daewoo.team5.hotelreservation.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/v1/wishlist")
@RequiredArgsConstructor
public class WishListController {

    private final WishListService wishListService;
    private final WishListRepository wishListRepository;

    @PostMapping("/{placeId}")
    public ApiResult<Boolean> addWishList(@PathVariable Long placeId, @AuthenticationPrincipal Long userId){
        return ApiResult.ok(
                wishListService.addWishList(placeId, userId),
                "찜 등록 성공"
        );
    }

    @DeleteMapping("/{placeId}")
    public ApiResult<Boolean> removeWishList(@PathVariable Long placeId, @AuthenticationPrincipal Long userId){
        return ApiResult.ok(
                wishListService.removeWishList(placeId, userId),
                "찜 삭제 성공"
        );
    }

    @GetMapping("/{placeId}")
    @AuthUser
    public ApiResult<Boolean> isWishList(@PathVariable Long placeId, UserProjection user){
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "인증 필요", "로그인이 필요합니다.");
        }
        return ApiResult.ok(
                wishListRepository.existsByUserIdAndPlaceId(user.getId(), placeId),
                "찜 여부 확인 성공"
        );
    }

}
