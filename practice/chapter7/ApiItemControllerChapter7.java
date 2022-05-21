package com.greglturnquist.hackingspringboot.reactive;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// HTML 대신에 데이터를 응답 본문에 직접 써서 반환하는 스프링 웹 컨트롤러
@RestController
public class ApiItemControllerChapter7 {
	
	private final ItemRepository itemRepository;
	
	public ApiItemControllerChapter7(ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}
	
	// 모든 Item을 반환하는 API
	@GetMapping("/api/items")
	Flux<Item> findAll(){
		return this.itemRepository.findAll();
	}
	
	// 한 개의 Item을 조회하는 API
	@GetMapping("/api/items/{id}")
	Mono<Item> findOne(@PathVariable String id){
		return this.itemRepository.findById(id);
	}
	
	// 새 Item을 생성하는 API
	@PostMapping("/api/items")
	Mono<ResponseEntity<?>> addNewItem(@RequestBody Mono<Item> item) {
		
		return item.flatMap(s -> this.itemRepository.save(s))
				.map(savedItem -> ResponseEntity
						.created(URI.create("/api/items/" + savedItem.getId()))
						.body(savedItem));
	}
	
	// 기존 Item 객체 교체
	@PutMapping("/api/items/{id}")
	public Mono<ResponseEntity<?>> updateItem(
			@RequestBody Mono<Item> item,
			@PathVariable String id) {
		
		return item
				.map(content -> new Item(id, content.getName(), content.getDescription(),
						content.getPrice()))
				.flatMap(this.itemRepository::save)
				.map(ResponseEntity::ok);
	}
}
