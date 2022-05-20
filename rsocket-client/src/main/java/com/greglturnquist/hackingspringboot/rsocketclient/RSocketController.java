package com.greglturnquist.hackingspringboot.rsocketclient;

import static io.rsocket.metadata.WellKnownMimeType.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.*;

import java.net.URI;
import java.time.Duration;

import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// @RestController가 붙은 클래스는 HTML을 렌더링하지 않는다.
@RestController
public class RSocketController {
	// Mono를 사용하므로 R소켓에 연결된 코드는 새 클라이언트가 구독할 때마다 호출된다.
	private final Mono<RSocketRequester> requester;
	
	public RSocketController(RSocketRequester.Builder builder) {
		this.requester = builder
				// dataMimeType()을 통해 데이터의 미디어 타입을 지정한다.
				.dataMimeType(APPLICATION_JSON)
				.metadataMimeType(parseMediaType(MESSAGE_RSOCKET_ROUTING.toString()))
				.connectTcp("localhost", 7000)
				.retry(5)
				.cache();
	}
	
	// 요청-응답 방식 R소켓에서 새 Item 추가 전송
	@PostMapping("/items/request-response")
	Mono<ResponseEntity<?>> addNewItemUsingRSocketRequestResponse(@RequestBody Item item)
	{
		return this.requester
				.flatMap(rSocketRequester -> rSocketRequester
						.route("newItems.request-response")
						.data(item)
						.retrieveMono(Item.class))
				.map(savedItem -> ResponseEntity.created(
						URI.create("/items/request-reponse")).body(savedItem));
	}
	
	// Item 목록 조회 요청을 요청-스트림 방식의 R소켓 서버에 전달
	@GetMapping(value = "/items/request-stream", produces = MediaType.APPLICATION_NDJSON_VALUE) 
	Flux<Item> findItemsUsingRSocketRequestStream() {
		return this.requester 
				.flatMapMany(rSocketRequester -> rSocketRequester 
						.route("newItems.request-stream") 
						.retrieveFlux(Item.class) 
						.delayElements(Duration.ofSeconds(1))); 
	}
	
	
	// 새 Item 저장 요청을 실행 후 망각 방식의 R소켓 서버에 전달
	@PostMapping("/items/fire-and-forget")
	Mono<ResponseEntity<?>> addNewItemUsingRSocketFireAndForget(@RequestBody Item item) {
		return this.requester 
				.flatMap(rSocketRequester -> rSocketRequester 
						.route("newItems.fire-and-forget") 
						.data(item) 
						.send()) 
				.then( 
						Mono.just( 
								ResponseEntity.created( 
										URI.create("/items/fire-and-forget")).build()));
	}

	// R소켓 채널을 사용해서 이벤트 입수 대기
	@GetMapping(value = "/items", produces = TEXT_EVENT_STREAM_VALUE) 
	Flux<Item> liveUpdates() {
		return this.requester 
				.flatMapMany(rSocketRequester -> rSocketRequester 
						.route("newItems.monitor") 
						.retrieveFlux(Item.class)); 
	}

}
