package com.greglturnquist.hackingspringboot.reactive;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

// @MockBean 애너테이션을 사용하기 위해 SpringExtension 등록
@ExtendWith(SpringExtension.class)
public class BlockHoundIntegrrationTest {
	
	// 테스트 대상 클래스 선언
	AltInventoryService altInventoryService;
	
	// 2개의 협력자 리포지토리를 가짜 객체로 대신
	@MockBean ItemRepository itemRepository;
	@MockBean CartRepository cartRepository;
	
	// 데이터 및 가짜 객체 설정
	@BeforeEach
	void setUp() {
		// 테스트 데이터 정의
		Item sampleItem = new Item("item1", "TV tray", "Alf TV tray", 19.99);
		CartItem sampleCartItem = new CartItem(sampleItem);
		Cart sampleCart = new Cart("My Cart", Collections.singletonList(sampleCartItem));
		
		// 협력자와의 가짜 상호작용 정의
		when(cartRepository.findById(anyString()))
		// 비어 있는 결과를 리액터로부터 감춤
		// cartRepository.findById()는 Mono.empty()를 반환한다.
		// Mono.empty()는 MonoEmpty 클래스의 싱글턴 객체를 반환한다.
		// 리액터는 이런 인스턴스를 감지하고 런타임에서 최적화한다.
		// block() 호출이 없으므로 블록하운드는 아무것도 검출하지 않고 지나간다.
		// 테스트 시나리오의 문제로 개발자는 장바구니가 없을 때도 문제없이 처리하기를 바라지만
		// 리액터는 필요하지 않다면 블로킹 호출을 알아서 삭제한다.
		// 이 문제를 해결하려면 MonoEmpty를 숨겨서 리액터의 최적화 루틴에 걸리지 않게 해야한다.
		// "Mono.hide()의 주목적은 진단을 정확하게 수행하기 위해 식별성 기준 최적화를 방지하는 것이다."
		.thenReturn(Mono.<Cart> empty().hide());
		
		when(itemRepository.findById(anyString())).thenReturn(Mono.just(sampleItem));
		when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(sampleCart));
		
		altInventoryService = new AltInventoryService(itemRepository, cartRepository);
		
	}
	// 테스트 안에서 블로킹 코드 검출
	@Test
	void blcokHoundShouldTrapBlockingCall() {
		Mono.delay(Duration.ofSeconds(1))
		.flatMap(tick -> altInventoryService.addItemToCart("My Cart", "item1"))
		.as(StepVerifier::create)
		.verifyErrorSatisfies(throwable -> {
			assertThat(throwable).hasMessageContaining(
					"block()/blockFirst()/blockLast() are blocking");
		});
	}
	
	// 블록하운드를 테스트 케이스에서 사용할 수 있도록 설정해, 테스트 코드에서 발생하는 블로킹 호출을 잡아낸다.
}
