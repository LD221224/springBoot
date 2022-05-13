package com.greglturnquist.hackingspringboot.reactive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

// @ExtendWith : 테스트 핸들러를 지정할 수 있는 JUnit 5의 API
// SpringExtension : 스프링에 특화된 테스트 기능을 사용할 수 있게 해줌
@ExtendWith(SpringExtension.class)
class InventoryServiceUnitTest {
	// 테스트 대상 클래스
	InventoryService inventoryService;

	// 테스트 대상 클래스인 InventoryService에 주입되는 협력자
	// 가짜 객체를 만들고 스프링 빈으로 등록하기 위해 @MockBean 애너테이션 붙임
	// 모키토를 사용해 가짜 객체를 만들고 이를 애플리케이션 컨텍스트에 빈으로 추가
	@MockBean private ItemRepository itemRepository;
	@MockBean private CartRepository cartRepository;

	// 목 객체 생성 코드 -> @MockBean으로 간결하고 명확하게 작성 가능 (위의 코드 17-18과 같다.)
	/**
	@BeforeEach
	void setUp() {
		itemRepository = mock(ItemRepository.class);
		cartRepository = mock(CartRepository);
	}
	 **/

	// JUnit 5의 애너테이션, 모든 테스트 메소드 실행 전에 테스트 준비 내용을 담고 있는 setUp() 메소드를 실행
	@BeforeEach
	void setUp() {
		// 테스트 데이터 정의 : 하나의 Item을 가지고 있는 Cart 객체를 만든다.
		Item sampleItem = new Item("item1", "TV tray", "Alf TV tray", 19.99);
		CartItem sampleCartItem = new CartItem(sampleItem);
		Cart sampleCart = new Cart("My Cart", Collections.singletonList(sampleCartItem));

		// 모키토를 사용해 가짜 객체와의 상호작용을 정의한다.
		when(cartRepository.findById(anyString())).thenReturn(Mono.empty());
		when(itemRepository.findById(anyString())).thenReturn(Mono.just(sampleItem));
		when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(sampleCart));

		// 가짜 협력자를 생성자에 주입하면서 테스트 대상 클래스를 생성한다.
		inventoryService = new InventoryService(itemRepository, cartRepository);
	}

	// 테스트 케이스 작성 - 탑 레벨 방식
	// 리액터 기반 함수를 최상위에서 호출, as(StepVerifier::create)를 이어서 호출
	@Test
	void addItemToEmptyCartShouldProduceOneCartItem() {
		// 테스트 대상 메소드인 InventoryService 클래스의 addItemToCart() 메소드를 실행한다.
		inventoryService.addItemToCart("My Cart", "item1")
		// 테스트 대상 메소드의 반환 타입인 Mono<Cart>를 
		// 리액터 테스트 모듈의 정적 메소드인 StepVerifier.create()에 메소드 레퍼런스로 연결하여
		// 테스트 기능을 전담하는 리액터 타입 핸들러를 생성한다.
		.as(StepVerifier::create)
		// expectNextMatches() 함수와 람다식을 사용해서 결과를 검증한다.
		.expectNextMatches(cart -> {
			// AssertJ를 사용해 각 장바구니에 담긴 상품의 개수를 추출하고
			// 장바구니에 한 가지 종류의 상품 한 개만 들어 있음을 단언한다.
			assertThat(cart.getCartItems()).extracting(CartItem::getQuantity)
			.containsExactlyInAnyOrder(1);
			// 각 장바구니에 담긴 상품을 추출해 한 개의 상품만 있음을 검증하고
			// 그 상품이 setUp() 메소드에서 정의한 데이터와 맞는지 검증한다.
			assertThat(cart.getCartItems()).extracting(CartItem::getItem)
			.containsExactly(new Item("item1", "TV tray", "Alf TV tray", 19.99));
			// expectNextMatches() 메소드는 불리언을 반환
			// 이 지점까지 통과했다면 true를 반환한다.
			return true;
		})
		// 마지막 단언(assertion)은 리액티브 스트림의 complete 시그널이 발생하고
		// 리액터 플로우가 성공적으로 완료됐음을 검증한다.
		.verifyComplete();
	}

	// 다른 방식 테스트 코드
	@Test
	void alternativeWayToTest() {
		StepVerifier.create(
				inventoryService.addItemToCart("My Cart", "item1"))
		.expectNextMatches(cart -> {
			assertThat(cart.getCartItems()).extracting(CartItem::getQuantity)
			.containsExactlyInAnyOrder(1);

			assertThat(cart.getCartItems()).extracting(CartItem::getItem)
			.containsExactly(new Item("item1", "TV tray", "Alf TV tray", 19.99));

			return true;
		})
		.verifyComplete();
	}
	
}
