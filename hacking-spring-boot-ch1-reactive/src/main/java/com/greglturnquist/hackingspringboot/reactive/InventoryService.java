package com.greglturnquist.hackingspringboot.reactive;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.mongodb.core.ReactiveFluentMongoOperations;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.byExample;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class InventoryService {
	private ItemRepository repository;
	private CartRepository cartRepository;
//	private ItemByExampleRepository exampleRepository;
	private ReactiveFluentMongoOperations fluentOperations;

	InventoryService(ItemRepository repository,
			CartRepository cartRepository,
//			ItemByExampleRepository exampleRepository,
			ReactiveFluentMongoOperations fluentOperations){
		this.repository = repository;
		this.cartRepository = cartRepository;
//		this.exampleRepository = exampleRepository;
		this.fluentOperations = fluentOperations;
	}

	Flux<Item> getItems(){
		return Flux.empty();
	}

	// 이름, 설명, AND 사용 여부를 모두 적용한 복잡한 필터링 구현
	Flux<Item> search(String partialName, String partialDescription, boolean useAnd) {
		if (partialName != null) {
			if (partialDescription != null) {
				if (useAnd) {
					return repository 
							.findByNameContainingAndDescriptionContainingAllIgnoreCase( //
									partialName, partialDescription);
				} else {
					return repository.findByNameContainingOrDescriptionContainingAllIgnoreCase( //
							partialName, partialDescription);
				}
			} else {
				return repository.findByNameContaining(partialName);
			}
		} else {
			if (partialDescription != null) {
				return repository.findByDescriptionContainingIgnoreCase(partialDescription);
			} else {
				return repository.findAll();
			}
		}
	}

//	// 복잡한 검색 요구 조건을 Example 쿼리로 구현한 코드
//	Flux<Item> searchByExample(String name, String description, boolean useAnd) {
//		// 검색어를 입력받아 새 Item 객체를 생성, price가 null일 수 없으므로 0.0 입력
//		Item item = new Item(name, description, 0.0);
//
//		// 사용자가 선택한 useAnd 값에 따라 3항 연산자로 분기하여 ExampleMatcher를 생성
//		ExampleMatcher matcher = (useAnd 
//				? ExampleMatcher.matchingAll() 
//				: ExampleMatcher.matchingAny()) 
//								// StringMatcher.CONTAINING : 부분 일치 검색 수행
//								.withStringMatcher(StringMatcher.CONTAINING)
//								// 대소문자 구분 안함
//								.withIgnoreCase() 
//								// ExampleMatcher는 기본적으로 null 필드 무시
//								// price에 null이 올 수 없으므로 price 필드가 무시되도록 명시적으로 지정
//								.withIgnorePaths("price"); 
//
//		// Item 객체와 matcher를 함계 Example.of(...)로 감싸서 Example을 생성
//		Example<Item> probe = Example.of(item, matcher); 
//
//		// 쿼리 실행
//		return exampleRepository.findAll(probe);
//	}
	
	Flux<Item> searchByExample(String name, String description, boolean useAnd) {
		Item item = new Item(name, description, 0.0); // <1>

		ExampleMatcher matcher = (useAnd // <2>
				? ExampleMatcher.matchingAll() //
				: ExampleMatcher.matchingAny()) //
						.withStringMatcher(StringMatcher.CONTAINING) // <3>
						.withIgnoreCase() // <4>
						.withIgnorePaths("price"); // <5>

		Example<Item> probe = Example.of(item, matcher); // <6>

		return repository.findAll(probe); // <7>
	}

	// 평문형 API를 사용한 item 검색
	Flux<Item> searchByFluentExample(String name, String description){
		return fluentOperations.query(Item.class)
				.matching(query(where("TV tray").is(name).and("Smurf").is(description)))
				.all();
	}
	
	// 평문형 API를 사용한 Example 쿼리 검색 구현 코드
	Flux<Item> searchByFluentExample(String name, String description, boolean useAnd) {
		Item item = new Item(name, description, 0.0);

		ExampleMatcher matcher = (useAnd 
				? ExampleMatcher.matchingAll() 
				: ExampleMatcher.matchingAny()) 
								.withStringMatcher(StringMatcher.CONTAINING) 
								.withIgnoreCase() 
								.withIgnorePaths("price");

		return fluentOperations.query(Item.class) //
				.matching(query(byExample(Example.of(item, matcher)))) 
				.all();
	}
	
	// addItemToCart()에 리액터 로깅 적용
	Mono<Cart> addItemToCart(String cartId, String itemId) {
		return this.cartRepository.findById(cartId)
				.log("foundCart")
				.defaultIfEmpty(new Cart(cartId))
				.log("emptyCart")
				.flatMap(cart -> cart.getCartItems().stream()
						.filter(cartItem -> cartItem.getItem()
								.getId().equals(itemId))
						.findAny()
						.map(cartItem -> {
							cartItem.increment();
							return Mono.just(cart).log("newCartItem");
						})
						.orElseGet(() -> {
							return this.repository.findById(itemId)
									.log("fetchedItem")
									.map(item -> new CartItem(item))
									.log("cartItem")
									.map(cartItem -> {
										cart.getCartItems().add(cartItem);
										return cart;
									}).log("addedCartItem");
						}))
				.log("cartWithAnotherItem")
				.flatMap(cart -> this.cartRepository.save(cart))
				.log("savedCart");
	}
}
