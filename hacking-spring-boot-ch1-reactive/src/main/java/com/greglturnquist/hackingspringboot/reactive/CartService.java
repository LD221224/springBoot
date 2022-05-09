package com.greglturnquist.hackingspringboot.reactive;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
class CartService {

	private final ItemRepository itemRepository;
	private final CartRepository cartRepository;

	CartService(ItemRepository itemRepository, CartRepository cartRepository){
		this.itemRepository = itemRepository;
		this.cartRepository = cartRepository;
	}

	Mono<Cart> addToCart(String cartId, String id){
		return this.cartRepository.findById(cartId)
				.defaultIfEmpty(new Cart(cartId))
				.flatMap(cart -> cart.getCartItems().stream()
						.filter(cartItem -> cartItem.getItem()
								.getId().equals(id))
						.findAny()
						.map(cartItem -> {
							cartItem.increment();
							return Mono.just(cart);
						})
						.orElseGet(() -> 
						this.itemRepository.findById(id)
						// 람다 함수 item -> new CartItem(item)를 
						// 메소드 레퍼런스 CartItem::new 로 대체
						// 메소드 레퍼런스를 인자로 받는 map() 메소드 이전에 수행된 메소드가 반환한 출력값이
						// new CartItem()의 입력값으로 사용됨
						.map(CartItem::new)
						.doOnNext(cartItem -> 
						cart.getCartItems().add(cartItem))
						.map(cartItem -> cart)))
				.flatMap(this.cartRepository::save);

	}
}
