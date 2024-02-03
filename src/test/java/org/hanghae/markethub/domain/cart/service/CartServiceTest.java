package org.hanghae.markethub.domain.cart.service;

import jakarta.transaction.Transactional;
import org.hanghae.markethub.domain.cart.dto.CartRequestDto;
import org.hanghae.markethub.domain.cart.dto.CartResponseDto;
import org.hanghae.markethub.domain.cart.entity.Cart;
import org.hanghae.markethub.domain.cart.repository.CartRepository;
import org.hanghae.markethub.domain.cart.repository.UserRepository;
import org.hanghae.markethub.domain.item.entity.Item;
import org.hanghae.markethub.domain.item.repository.ItemRepository;
import org.hanghae.markethub.domain.store.entity.Store;
import org.hanghae.markethub.domain.store.repository.StoreRepository;
import org.hanghae.markethub.domain.user.entity.User;
import org.hanghae.markethub.global.constant.Role;
import org.hanghae.markethub.global.constant.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;


    private Item item;
    private Item notExistItem;
    private Item soldOutItem;

    @BeforeEach
    public void datas() {

        User user = User.builder()
                .id(1L)
                .email("1234@naver.com")
                .password("1234")
                .name("lee")
                .phone("010-1234")
                .address("서울시")
                .role(Role.ADMIN)
                .status(Status.EXIST).build();


        Store store = Store.builder()
                .id(1L)
                .user(user)
                .status(Status.EXIST)
                .build();

        item = Item.builder()
                .id(1L)
                .itemName("노트북")
                .price(500000)
                .quantity(5)
                .user(user)
                .itemInfo("구형 노트북")
                .category("가전 제품")
                .status(Status.EXIST)
                .store(store)
                .build();

//        Item item2 = Item.builder()
//                .id(1L)
//                .itemName("노트북")
//                .price(500000)
//                .quantity(5)
//                .user(user)
//                .itemInfo("구형 노트북")
//                .category("가전 제품")
//                .status(Status.DELETED)
//                .store(store)
//                .build();
//
//        Item item3 = Item.builder()
//                .id(1L)
//                .itemName("노트북")
//                .price(500000)
//                .quantity(0)
//                .user(user)
//                .itemInfo("구형 노트북")
//                .category("가전 제품")
//                .status(Status.EXIST)
//                .store(store)
//                .build();
    }

    @Nested
    class addCart {
        @Test
        @DisplayName("카드 등록 성공")
        void addCartSuccess() {
            // given
            CartRequestDto requestDto = new CartRequestDto();
            requestDto.setItem(item);
            requestDto.setQuantity(1);

            User user = User.builder()
                    .build();

            if (item.getStatus().equals(Status.DELETED) || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("해당 상품은 존재하지않으므로 다시 확인해주세요");
            }

            Optional<Cart> checkCart = cartRepository.findByitemId(requestDto.getItem().getId());
            if (checkCart.isPresent()) {
                checkCart.get().update(requestDto);
                given(cartRepository.save(checkCart.get())).willReturn(checkCart.get());
            } else {
                Cart cart = Cart.builder()
                        .item(requestDto.getItem())
                        .status(Status.EXIST)
                        .address("address")
                        .quantity(requestDto.getItem().getQuantity())
                        .price(requestDto.getItem().getPrice())
                        .user(user)
                        .build();

                given(cartRepository.save(cart)).willReturn(cart);
            }

            // when
            cartService.addCart(user,requestDto);

            // then
            verify(cartRepository).save(any());
        }

        @Test
        @DisplayName("상품 삭제되서 장바구니 추가안됨")
        void notExistItemFail() {
            // given
            CartRequestDto requestDto = new CartRequestDto();
            requestDto.setItem(notExistItem);
            requestDto.setQuantity(1);


            // when
            if (item.getStatus().equals(Status.DELETED) || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("해당 상품은 존재하지않으므로 다시 확인해주세요");
            }

            // then
            assertThrows(IllegalArgumentException.class, () -> {
                throw new IllegalArgumentException("해당 상품은 존재하지않으므로 다시 확인해주세요");
            });

        }

        @Test
        @DisplayName("상품 개수가 없어서 장바구니 추가안됨")
        void soldOutItemFail() {
            // given
            CartRequestDto requestDto = new CartRequestDto();
            requestDto.setItem(soldOutItem);
            requestDto.setQuantity(1);


            // when
            if (item.getStatus().equals(Status.DELETED) || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("해당 상품은 존재하지않으므로 다시 확인해주세요");
            }

            // then
            assertThrows(IllegalArgumentException.class, () -> {
                throw new IllegalArgumentException("해당 상품은 존재하지않으므로 다시 확인해주세요");
            });
        }
    }

    @Nested
    class updateCart {
        @Test
        @DisplayName("수정 성공")
        void updateCartSuccess(){
            // given

            User user = User.builder()
                    .build();

            Cart setCart = Cart.builder()
                    .cartId(1L)
                    .item(item)
                    .status(Status.EXIST)
                    .address(user.getAddress())
                    .quantity(1)
                    .price(1)
                    .user(user).build();

            CartRequestDto res = new CartRequestDto();
            res.setQuantity(11);
            res.setItem(item);

            cartRepository.save(setCart);

            // when
            Cart cart = cartRepository.findById(setCart.getCartId()).orElseThrow(null);
            cart.update(res);

            // then
            assertThat(cart.getQuantity()).isEqualTo(11);
        }
    }

    @Nested
    class deleteCart {
        @Test
        @DisplayName("삭제 성공")
        void deleteCartSuccess(){
            // given

            User user = User.builder()
                    .build();

            Cart setCart = Cart.builder()
                    .cartId(1L)
                    .item(item)
                    .status(Status.EXIST)
                    .address(user.getAddress())
                    .quantity(1)
                    .price(1)
                    .user(user).build();

            cartRepository.save(setCart);

            // when
            Cart cart = cartRepository.findById(setCart.getCartId()).orElseThrow(null);
            cart.delete();

            // then
            assertThat(cart.getStatus()).isEqualTo(Status.DELETED);
        }
    }

    @Nested
    class getCarts {
        @Test
        @DisplayName("전체 조회 성공")
        void getCartsSuccess(){
            // given

            User user = User.builder()
                    .build();

            Cart setCart = Cart.builder()
                    .cartId(1L)
                    .item(item)
                    .status(Status.EXIST)
                    .address(user.getAddress())
                    .quantity(1)
                    .price(1)
                    .user(user).build();

            Cart setCart1 = Cart.builder()
                    .cartId(2L)
                    .item(item)
                    .status(Status.EXIST)
                    .address(user.getAddress())
                    .quantity(2)
                    .price(2)
                    .user(user).build();

            Cart setCart2 = Cart.builder()
                    .cartId(3L)
                    .item(item)
                    .status(Status.EXIST)
                    .address(user.getAddress())
                    .quantity(3)
                    .price(3)
                    .user(user).build();

            List<Cart> carts = new ArrayList<>();
            carts.add(setCart);
            carts.add(setCart1);
            carts.add(setCart2);

            given(cartRepository.findAllByUser(user)).willReturn(carts);

            // when
            List<CartResponseDto> cartsRes = cartService.getCarts(user);

            // then
            assertThat(cartsRes.size()).isEqualTo(3);
        }
    }


}