package com.greglturnquist.hackingspringboot.rsocketserver;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

@Controller
public class RSocketService {

	private final ItemRepository repository;
	
	private final EmitterProcessor<Item> itemProcessor;
	private final FluxSink<Item> itemSink;
	
	public RSocketService(ItemRepository repository) {
		this.repository = repository;
		
		// 새 프로세서 생성
		this.itemProcessor = EmitterProcessor.create();
		// EmitterProcessor에 새 Item을 추가하기 위한 진입점
		this.itemSink = this.itemProcessor.sink();
	}
	
// 요청-응답 R소켓 익스체인지
	// 도착지가 newItems.request-response로 지정된 R소켓 메시지를 이 메소드로 라우팅
	@MessageMapping("newItems.request-response")
	// 스프링 메시징은 메시지가 들어오기를 리액티브하게 기다리고 있다가
	// 메시지가 들어오면 메시지 본문을 인자로 해서 save() 메소드를 호출한다.
	// 반환 타입은 도메인 객체를 Item을 포함하는 리액터 타입이며, 이는 요청하는 쪽에서 예상하는 응답 메시지 시그니처와 일치한다.
	public Mono<Item> processNewItemViaRSocketRequestResponse(Item item) {
		// Item 객체에 대한 정보를 담고 있는 메시지를 받았으므로 비즈니스 로직을 수행할 차례다.
		// 리액티브 리포지토리를 통해 Item 개게를 몽고디비에 저장한다.
		return this.repository.save(item)
				// doOnNext()를 호출해 새로 지정된 Item 객체를 가져와
				// 싱크를 통해 FluxProcessor로 내보낸다.
				.doOnNext(savedItem -> this.itemSink.next(savedItem));
	}
	
// 요청-스트림 R소켓 익스체인지
	// 도착지가 newItems.request-stream으로 지정된 R소켓 메시지를 이 메소드로 라우팅
	@MessageMapping("newItems.request-stream")
	// 메시지가 들어오면 Item 목록을 조회한 후 Flux에 담아 반환한다.
	public Flux<Item> findItemsViaRSocketRequestStream() {
		// 몰고디비에 저장된 모든 Item을 조회해서 Flux에 담아 반환한다.
		return this.repository.findAll()
				// doOnNext()를 호출해 조회한 Item 객체를 싱크를 통해 FluxProcessor로 내보낸다.
				.doOnNext(this.itemSink::next);
	}
	
// 실행 후 망각 R소켓 익스체인지
	@MessageMapping("newItems.fire-and-forget")
	public Mono<Void> processNewItemsViaRSocketFireAndForget(Item item) {
		return this.repository.save(item) //
				.doOnNext(savedItem -> this.itemSink.next(savedItem)) 
				.then();
	}

// R소켓 익스테인지 채널 모니터링
	@MessageMapping("newItems.monitor") 
	public Flux<Item> monitorNewItems() { 
		return this.itemProcessor;
	}
}
