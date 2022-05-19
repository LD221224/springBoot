package com.greglturnquist.hackingspringboot.reactive;

import static org.assertj.core.api.Assertions.*;

import reactor.test.StepVerifier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// 자동설정, 환경설정 값 읽기, 내장 웹 컨테이너 등 테스트를 위한 애플리케이션 구동에 필요한 모든 것을 활성화한다.
// 실제 운영환경을 흉내 낸 가짜(mock) 환경을 사용한다.
@SpringBootTest
// 테스트용으로 사용하는 WebClient인 WebTestClient를 자동설정한다.
@AutoConfigureWebTestClient
// 테스트컨테이너를 테스트에 사용할 수 있게 해준다.
@Testcontainers
// 지정한 클래스를 테스트 실행 전에 먼저 애플리케이션 컨텍스트에 로딩해준다.
@ContextConfiguration
public class RabbitTest {
	
	// 테스트에 사용할 RabbitMQContainer를 생성한다.
	// 테스트에 사용할 래빗엠큐 인스턴스를 관리한다.
	@Container static RabbitMQContainer container = new RabbitMQContainer("rabbitmq:3.7.25-management-alpine");
	
	// 테스트에 사용할 WebTestClient를 주입받는다.
	@Autowired WebTestClient webTestClient;
	
	// 요청 처리 결과 확인에 사용할 ItemRepository를 주입받는다.
	@Autowired ItemRepository itemRepository;
	
	// 환결설정 내용을 Environment에 동적으로 추가한다.
	// 래빌엠큐 연결 세부정보를 테스트컨테이너에서 읽어와 스프링 AMQP에서 사용할 수 있도록 스프링 부트 환경설정 정보에 저장한다.
	@DynamicPropertySource
	static void configure(DynamicPropertyRegistry registry) {
		registry.add("spring.rabbitmq.host", container::getContainerIpAddress);
		registry.add("spring.rabbitmq.port", container::getAmqpPort);
	}
	
	/**
	 * 웹 컨트롤러 처리
	 * 1 새 Item 객체를 생성하기 위해 Item 데이터가 담겨 있는 HTTP POST 요청을 받는다.
	 * 2 Item 데이터를 적절한 메시지로 변환한다.
	 * 3 Item 생성 메시지를 브로커에게 전송한다.
	 * 
	 * 브로커
	 * 1 새 메시지를 받을 준비를 하고 기다린다.
	 * 2 새 메시지가 들어오면 꺼낸다.
	 * 3 몽고디비에 저장한다.
	 */

	@Test
	void verifyMessagingThroughAmqp() throws InterruptedException {
		// 새 Item 데이터를 /items에 POST로 요청한다.
		// 요청에 대한 응답으로 HTTP 201 Created 상태 코드가 반환되는 것을 확인한다.
		this.webTestClient.post().uri("/items")
		.bodyValue(new Item("Alf alarm clock", "nothing important", 19.99))
		.exchange()
		.expectStatus().isCreated()
		.expectBody();
		
		// 1500밀리초 동안 sleep() 처리해 해당 메시지가 브로커를 거쳐 데이터 저장소에 저장될 때까지 기다린다.
		// 테스트에 사용되는 메시지 처리 순서 맞추기
		Thread.sleep(1500L);
		
		// 두 번째 Item 데이터를 보내고 HTTP 201 Created 상태 코드가 반환되는 것을 확인한다.
		this.webTestClient.post().uri("/items") 
		.bodyValue(new Item("Smurf TV tray", "nothing important", 29.99)) 
		.exchange() 
		.expectStatus().isCreated() 
		.expectBody();

		Thread.sleep(2000L); 

		// ItemRepository를 사용해 몽고디비에 쿼리를 날려 2개의 Item 객체가 저장되었는지 확인한다.
		this.itemRepository.findAll() 
		.as(StepVerifier::create) 
		.expectNextMatches(item -> {
			assertThat(item.getName()).isEqualTo("Alf alarm clock");
			assertThat(item.getDescription()).isEqualTo("nothing important");
			assertThat(item.getPrice()).isEqualTo(19.99);
			return true;
		}) 
		.expectNextMatches(item -> {
			assertThat(item.getName()).isEqualTo("Smurf TV tray");
			assertThat(item.getDescription()).isEqualTo("nothing important");
			assertThat(item.getPrice()).isEqualTo(29.99);
			return true;
		}) 
		.verifyComplete();
	}
}
