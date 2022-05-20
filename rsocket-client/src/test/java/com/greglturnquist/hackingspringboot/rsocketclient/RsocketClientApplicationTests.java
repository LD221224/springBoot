package com.greglturnquist.hackingspringboot.rsocketclient;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.greglturnquist.hackingspringboot.rsocketclient.Item;
import com.greglturnquist.hackingspringboot.rsocketclient.ItemRepository;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// 스프링 부트 애플리케이션 전체를 실행하면서 테스트를 진행
@SpringBootTest
// WebTestClient 인스턴스를 생성해 테스트에 활용할 수 있게 해준다.
@AutoConfigureWebTestClient
class RsocketClientApplicationTests {
	
	// WebTestClient를 자동으로 주입받는다.
	@Autowired WebTestClient webTestClient;
	// ItemRepository를 주입받아서 R소켓 연산을 통해 몽고디비 데이터를 조회할 수 있다.
	@Autowired ItemRepository repository;

	@Test
	void contextLoads() {
	}
	
	// R소켓 요청-응답 상호작용 테스트
	@Test
	void verifyRemoteOperationsThroughRSocketRequestResponse() throws InterruptedException {
		
		//데이터 초기화, 실제 몽고디비 데이터 스토어를 사용하므로 불필요한 데이터 먼저 삭제
		this.repository.deleteAll()
		.as(StepVerifier::create)
		.verifyComplete();
		
		// 새 Item 생성
		this.webTestClient.post().uri("/items/request-response")
		.bodyValue(new Item("Alf alarm clock", "nothing important", 19.99))
		.exchange()
		// HTTP 201 Created가 반환되는지 검증한다.
		.expectStatus().isCreated()
		.expectBody(Item.class)
		.value(item -> {
			assertThat(item.getId()).isNotNull();
			assertThat(item.getName()).isEqualTo("Alf alarm clock");
			assertThat(item.getDescription()).isEqualTo("nothing important");
			assertThat(item.getPrice()).isEqualTo(19.99);
		});
		
		// 스레드를 잠시 중지해 새 Item이 R소켓 서버를 거쳐 몽고디비에 저장될 시간 여유를 둔 후
		// 몽고디비에 쿼리를 전송해 데이터 저장 여부를 확인한다.
		Thread.sleep(500);
		
		// Item이 몽고디비에 저장됐는지 확인
		this.repository.findAll()
		.as(StepVerifier::create)
		.expectNextMatches(item -> {
			assertThat(item.getId()).isNotNull();
			assertThat(item.getName()).isEqualTo("Alf alarm clock");
			assertThat(item.getDescription()).isEqualTo("nothing important");
			assertThat(item.getPrice()).isEqualTo(19.99);
			return true;
		})
		.verifyComplete();
	}
	
	// R소켓 요청-스트림 상호작용 테스트
	@Test
	void verifyRemoteOperationsThroughRSocketRequestStream() //
			throws InterruptedException {
		// Clean out the database
		this.repository.deleteAll().block();

		// Create 3 new "item"s
		List<Item> items = IntStream.rangeClosed(1, 3)
				// 테스트 데이터인 3개의 Item을 생성해 List에 저장한다.
				.mapToObj(i -> new Item("name - " + i, "description - " + i, i))
				.collect(Collectors.toList());

		this.repository.saveAll(items).blockLast(); // <3>


		// Get stream
		this.webTestClient.get().uri("/items/request-stream")
				.accept(MediaType.APPLICATION_NDJSON) 
				.exchange() 
				.expectStatus().isOk()
				// 응답 본문을 통해 넘어오는 데이터를 Item 타입으로 받아
				// StepVerifier를 통해 검증할 수 있도록 체이닝 플로우에서 빠져나온다.
				.returnResult(Item.class)
				// 응답 본문을 Flux로 반환한다.
				.getResponseBody()
				.as(StepVerifier::create)
				// Flux를 통해 제공받은 Item의 값을 itemPredicate() 메소드를 사용해 검증한다.
				.expectNextMatches(itemPredicate("1"))
				.expectNextMatches(itemPredicate("2"))
				.expectNextMatches(itemPredicate("3"))
				// 3개의 데이터를 생성해 저장 후 조회했으므로
				// 세 번의 값 검증 후에 스트림이 완료되는 것을 확인한다.
				.verifyComplete(); 
	}

	private Predicate<Item> itemPredicate(String num) {
		return item -> {
			assertThat(item.getName()).startsWith("name");
			assertThat(item.getName()).endsWith(num);
			assertThat(item.getDescription()).startsWith("description");
			assertThat(item.getDescription()).endsWith(num);
			assertThat(item.getPrice()).isPositive();
			return true;
		};
	}
	
	// R소켓 실행 후 망각 테스트
	@Test
	void verifyRemoteOperationsThroughRSocketFireAndForget() throws InterruptedException {

		// Clean out the database
		this.repository.deleteAll() 
				.as(StepVerifier::create) 
				.verifyComplete();

		// Create a new "item"
		this.webTestClient.post().uri("/items/fire-and-forget")
				.bodyValue(new Item("Alf alarm clock", "nothing important", 19.99)) 
				.exchange() 
				.expectStatus().isCreated() 
				.expectBody().isEmpty(); 

		Thread.sleep(500); 

		// Verify the "item" has been added to MongoDB
		this.repository.findAll() 
				.as(StepVerifier::create) 
				.expectNextMatches(item -> {
					assertThat(item.getId()).isNotNull();
					assertThat(item.getName()).isEqualTo("Alf alarm clock");
					assertThat(item.getDescription()).isEqualTo("nothing important");
					assertThat(item.getPrice()).isEqualTo(19.99);
					return true;
				}) 
				.verifyComplete();
	}

}
