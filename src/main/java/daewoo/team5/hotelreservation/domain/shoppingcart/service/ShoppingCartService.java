package daewoo.team5.hotelreservation.domain.shoppingcart.service;

import daewoo.team5.hotelreservation.domain.place.entity.RoomEntity;
import daewoo.team5.hotelreservation.domain.place.repository.RoomRepository;
import daewoo.team5.hotelreservation.domain.shoppingcart.entity.ShoppingCartEntity;
import daewoo.team5.hotelreservation.domain.shoppingcart.projection.CartProjection;
import daewoo.team5.hotelreservation.domain.shoppingcart.repository.ShoppingCartRepository;
import daewoo.team5.hotelreservation.domain.users.entity.UsersEntity;
import daewoo.team5.hotelreservation.domain.users.repository.UsersRepository;
import daewoo.team5.hotelreservation.global.exception.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final UsersRepository usersRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public Boolean addToCart(Long roodId, Long userId, LocalDate startDate, LocalDate endDate, int quantity) {
        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "존재하지 않는 유저", "유저가 존재하지 않습니다."));

        RoomEntity room = roomRepository.findById(roodId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "존재하지 않는 숙소", "숙소가 존재하지 않습니다."));

        Optional<ShoppingCartEntity> existing = shoppingCartRepository
                .findByUser_IdAndRoom_IdAndStartDateAndEndDate(userId, roodId, startDate, endDate);

        if (existing.isPresent()) {
            ShoppingCartEntity cart = existing.get();
            cart.setQuantity(cart.getQuantity() + quantity);
            shoppingCartRepository.save(cart);
        } else {
            ShoppingCartEntity cart = ShoppingCartEntity.builder()
                    .user(user)
                    .room(room)
                    .startDate(startDate)
                    .endDate(endDate)
                    .quantity(quantity)
                    .build();
            shoppingCartRepository.save(cart);
        }

        return true;
    }

    @Transactional
    public boolean removeFromCart(Long cartId, Long userId) {
        ShoppingCartEntity cart = shoppingCartRepository.findById(cartId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "장바구니 없음", "해당 장바구니 항목이 존재하지 않습니다."));

        if (!cart.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "권한 없음", "본인 장바구니만 삭제할 수 있습니다.");
        }

        shoppingCartRepository.delete(cart);
        return true;
    }

    public Integer getCartItemCount(Long userId) {
        return shoppingCartRepository.countByUser_Id(userId);
    }

    public List<CartProjection> getCartItems(Long userId) {
        return shoppingCartRepository.findCartItemsByUserId(userId);
    }

    public boolean updateQuantity(Long cartId, Long userId, int quantity) {
        ShoppingCartEntity cart = shoppingCartRepository.findById(cartId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "장바구니 없음", "해당 장바구니 항목이 존재하지 않습니다."));

        if (!cart.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "권한 없음", "본인 장바구니만 수정할 수 있습니다.");
        }

        cart.setQuantity(quantity);
        shoppingCartRepository.save(cart);
        return true;
    }
}
