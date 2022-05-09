package com.greglturnquist.hackingspringboot.reactive;

// Mono는 0 또는 1개의 원소만 담을 수 있는 리액티브 발행자(publisher)
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;

// JSON이나 XML 같은 데이터가 아닌 템플릿을 사용한 웹 페이지를 반환하는 스프링 웹 컨트롤러
@Controller
public class HomeController {

	private CartService cartService;
	private InventoryService inventoryService;

	private ItemRepository itemRepository;
//	private ItemByExampleRepository exampleRepository;
	private CartRepository cartRepository;

	// 생성자 주입 : 스프링이 itemRepository와 cartRepository를 생성자를 통해 주입
	public HomeController(
			CartService cartService, 
			InventoryService inventoryService,
			ItemRepository itemRepository, 
//			ItemByExampleRepository exampleRepository,
			CartRepository cartRepository) {
		
		this.cartService = cartService;
		this.inventoryService = inventoryService;
		this.itemRepository = itemRepository;
//		this.exampleRepository = exampleRepository;
		this.cartRepository = cartRepository;
	}

	// GET 요청을 처리함, 기본값 '/'
	@GetMapping
	// Mono<Rendering> : 뷰/애트리뷰트를 포함하는 웹플럭스 컨테이너
	Mono<Rendering> home(){
		// view(...) : 렌더링에 사용할 템플릿 이름 지정
		return Mono.just(Rendering.view("home.html") 
				// modelAttribute(...) : 템플릿에 사용될 데이터 지정
				.modelAttribute("items", this.itemRepository.findAll())
				// findById(...).defaultIfEmpty(...)
				// : 몽고디비에서 장바구니를 조회해서 없으면 새로운 Cart 생성
				// 전형적인 리액터 사용법
				.modelAttribute("cart", this.cartRepository.findById("My Cart")
						.defaultIfEmpty(new Cart("My cart")))
				.build()
				);
	}

	// 장바구니에 상품 추가	
	@PostMapping("/add/{id}")
	Mono<String> addToCart(@PathVariable String id) {
		return this.cartService.addToCart("My Cart", id)
				.thenReturn("redirect:/");
		/* 장바구니를 조회하고 상품을 담는 기능을 서비스로 추출
		return this.cartRepository.findById("My Cart")
				.defaultIfEmpty(new Cart("My Cart"))
//				CartItem을 순회하면서 새로 장바구니에 담은 것과 동일한 종류의 상품이 이미 있는지 확인
//				findAny()는 Optional<cartItem>를 반환하며 
//				같은 상품이 있다면 map() 내부에서 해당 상품의 수량만 증가시켜 장바구니를 Mono에 담아 반환
				.flatMap(cart -> cart.getCartItems().stream()
						.filter(cartItem -> cartItem.getItem()
								.getId().equals(id))
						.findAny()
						.map(cartItem -> {
							cartItem.increment();
							return Mono.just(cart);
						})
//						같은 상품이 없는 경우, 몽고디비에서 해당 상품을 조회한 후 수량을 1로 지정하여
//						CartItem에 담은 다음, CartItem을 장바구니에 추가한 후, 장바구니를 반환
						.orElseGet(() -> {
							return this.itemRepository.findById(id)
									.map(item -> new CartItem(item))
									.map(cartItem -> {
										cart.getCartItems().add(cartItem);
										return cart;
									});
						}))
//				업데이트된 장바구니를 몽고디비에 저장
				.flatMap(cart -> this.cartRepository.save(cart))
				.thenReturn("redirect:/");
		 */

	}

//	// Example 쿼리로 구현된 서비스를 사용하는 웹 컨트롤러
//	@GetMapping("/search")
//	Mono<Rendering> search(
//			@RequestParam(required = false) String name,
//			@RequestParam(required = false) String description,
//			@RequestParam boolean useAnd) {
//		return Mono.just(Rendering.view("home.html")
//				.modelAttribute("results",
//						inventoryService.searchByExample(name, description, useAnd))
//				.build());
//	}
	
	@PostMapping
	Mono<String> createItem(@ModelAttribute Item newItem) {
		return this.itemRepository.save(newItem) //
				.thenReturn("redirect:/");
	}

	@DeleteMapping("/delete/{id}")
	Mono<String> deleteItem(@PathVariable String id) {
		return this.itemRepository.deleteById(id) //
				.thenReturn("redirect:/");
	}

	// tag::search[]
	@GetMapping("/search") // <1>
	Mono<Rendering> search( //
			@RequestParam(required = false) String name, // <2>
			@RequestParam(required = false) String description, //
			@RequestParam boolean useAnd) {
		return Mono.just(Rendering.view("home.html") // <3>
				.modelAttribute("items", //
						inventoryService.searchByExample(name, description, useAnd)) // <4>
				.modelAttribute("cart", //
						this.cartRepository.findById("My Cart")
								.defaultIfEmpty(new Cart("My Cart")))
				.build());
	}
}
