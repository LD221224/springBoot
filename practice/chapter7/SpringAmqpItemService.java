package com.greglturnquist.hackingspringboot.reactive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
// 리액티브 방식으로 AMQP 메시지 사용
public class SpringAmqpItemService {

	private static final Logger log =
			LoggerFactory.getLogger(SpringAmqpItemService.class);
	
	private final ItemRepository itemRepository;
	
	public SpringAmqpItemService(ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}
	
	// 래빗엠큐 메시지 리스너 등록
	@RabbitListener(
			ackMode = "MANUAL",
			// 큐를 익스테인지에 바인딩하는 방법을 지정
			bindings = @QueueBinding(
					// 임의의 지속성 없는 익명 큐 생성
					value = @Queue,
					// 이 큐와 연결될 익스체인지를 지정
					exchange = @Exchange("hacking-spring-boot"),
					// 라우팅 키 지정
					key = "new-items-spring-amqp"))
	// @RabbitListener에서 지정한 내용에 맞는 메시지가 들어오면
	// processNewItemViaSpringAmqp(Item item)이 실행되며
	// 메시지에 들어 있는 Item 데이터는 item 변수를 통해 전달된다.
	public Mono<Void> processNewItemViaSpringAmqp(Item item) {
		log.debug("Consuming => " + item);
		// Item 객체가 몽고디비에 저장된다.
		// 반환 타입이 리액터 타입인 Mono이므로
		// then()을 호출해 저장이 완료될 때까지 기다린다.
		return this.itemRepository.save(item).then();
	}
}
