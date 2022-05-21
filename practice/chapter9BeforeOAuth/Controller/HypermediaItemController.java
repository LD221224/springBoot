package com.greglturnquist.hackingspringboot.reactive;

import static org.springframework.hateoas.mediatype.alps.Alps.*;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.alps.Alps;
import org.springframework.hateoas.mediatype.alps.Type;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// 하이퍼미디어 API 정의
@RestController
public class HypermediaItemController {
	
	private final ItemRepository itemRepository;
	
	public HypermediaItemController(ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}
	
	@GetMapping("/hypermedia")
	Mono<RepresentationModel<?>> root() {
		HypermediaItemController controller = //
				methodOn(HypermediaItemController.class);

		Mono<Link> selfLink = linkTo(controller.root()).withSelfRel().toMono();

		Mono<Link> itemsAggregateLink = //
				linkTo(controller.findAll()) //
						.withRel(IanaLinkRelations.ITEM) //
						.toMono();

		return selfLink.zipWith(itemsAggregateLink) //
				.map(links -> Links.of(links.getT1(), links.getT2())) //
				.map(links -> new RepresentationModel<>(links.toList()));
	}
	
	@GetMapping("/hypermedia/items")
	Mono<CollectionModel<EntityModel<Item>>> findAll() {

		return this.itemRepository.findAll() 
				.flatMap(item -> findOne(item.getId())) 
				.collectList() //
				.flatMap(entityModels -> linkTo(methodOn(HypermediaItemController.class) 
						.findAll()).withSelfRel() 
								.toMono() 
								.map(selfLink -> CollectionModel.of(entityModels, selfLink)));
	}
	
	// 한 개의 Item 객체에 대한 하이퍼미디어 생성
	@GetMapping("/hypermedia/items/{id}")
	Mono<EntityModel<Item>> findOne(@PathVariable String id) {
		// 스프링 헤이티오스의 정적 메소드인 methodOn() 연산자를 사용해 컨트롤러에 대한 프록시를 생성한다.
		HypermediaItemController controller = methodOn(HypermediaItemController.class);
		
		// linkTo() 연산자를 사용해 컨트롤러의 findOne() 메소드에 대한 링크를 생성한다.
		// 현재 메소드가 findOne() 메소드이므로 self라는 이름의 링크를 추가하고
		// 리액터 Mono에 담아 반환한다.
		Mono<Link> selfLink = linkTo(controller.findOne(id)).withSelfRel().toMono();
		
		// 모든 상품을 반환하는 findAll() 메소드를 찾아서 애그리것 루트에 대한 링크를 생성한다.
		// IANA 표중에 따라 링크 이름을 item으로 명명한다.
		Mono<Link> aggregateLink = linkTo(controller.findAll())
				.withRel(IanaLinkRelations.ITEM).toMono();
		
		// 여러 개의 비동기 요청을 실행하고 각 결과를 하나로 합치기 위해 Mono.zip() 메소드를 사용한다.
		// findById() 메소드 호출과 selfLink, aggregateLink 생성 요청 결과를 
		// 타입 안정성이 보장되는 리액터 Tuple 타입에 넣고 Mono로 감싸서 반환한다.
		return Mono.zip(itemRepository.findById(id), selfLink, aggregateLink)
				// 마지막으로 map()을 통해 Tuple에 담겨 있던 여러 비동기 요청 결과를 꺼내
				// EntityModel을 만들고 Mono로 감싸서 반환한다.
				.map(o -> EntityModel.of(o.getT1(), Links.of(o.getT2(), o.getT3())));
	}
	
	// 메타데이터를 포함한 ALPS 프로파일 생성
	@GetMapping(value = "/hypermedia/items/profile", produces = MediaTypes.ALPS_JSON_VALUE)
	public Alps profile() {
		return alps() //
				.descriptor(Collections.singletonList(descriptor() //
						.id(Item.class.getSimpleName() + "-repr") //
						.descriptor(Arrays.stream( //
								Item.class.getDeclaredFields()) //
								.map(field -> descriptor() //
										.name(field.getName()) //
										.type(Type.SEMANTIC) //
										.build()) //
								.collect(Collectors.toList())) //
						.build())) //
				.build();
	}

}
