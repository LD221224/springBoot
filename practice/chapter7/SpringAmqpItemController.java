package com.greglturnquist.hackingspringboot.reactive;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

// JSON 형태로 데이터를 입출력
@RestController
// AMQP 메시징을 처리할 수 있는 리액티브 컨트롤러
public class SpringAmqpItemController {
	
	private static final Logger log = 
			LoggerFactory.getLogger(SpringAmqpItemController.class);
	
	private final AmqpTemplate template;
	
	public SpringAmqpItemController(AmqpTemplate template) {
		this.template = template;
	}
	
	// 리액티브 컨트롤러에서 AMQP 메시지 전송
	// /items로 들어오는 POST 요청 처리
	@PostMapping("/items")
	// @RequestBody : 스프링 웹플럭스에게 요청 본문에서 데이터를 추출하도록 지시한다.
	Mono<ResponseEntity<?>> addNewItemUsingSpringAmqp(@RequestBody Mono<Item> item) {
		return item
				// subscribeOn()을 통해 바운디드 엘라스틱 스케줄러에서 관리하는 별도의 스레드에서 실행되게 만든다.
				.subscribeOn(Schedulers.boundedElastic())
				.flatMap(content -> {
					return Mono
							// 람다식을 사용해 AmqpTemplate 호출을 Callable로 감싸고 
							// Mono.fromCallable()을 통해 Mono를 생성한다.
							.fromCallable(() -> {
								// AmqpTemplate의 convertAndSend()를 호출해
								// Item 데이터를 new-items-spring-amqp라는 라우팅 키와 함께
								// hacking-spring-boot 익스체인지로 전송한다.
								this.template.convertAndSend(
										"hacking-spring-boot", "new-items-spring-amqp", content);
								// 새로 생성되어 추가된 Item 객체에 대한 URI를 location 헤더에 담아
								// HTTP 201 Created 상태 코드와 함께 반환한다.
								return ResponseEntity.created(URI.create("/items")).build();
							});
				});
	}

}
