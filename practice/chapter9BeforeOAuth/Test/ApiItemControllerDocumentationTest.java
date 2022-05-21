package com.greglturnquist.hackingspringboot.reactive;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = ApiItemController.class)
// 스프링 레스트 독 사용에 필요한 내용을 자동으로 설정해준다.
@AutoConfigureRestDocs
public class ApiItemControllerDocumentationTest {
	
	@Autowired private WebTestClient webTestClient;
	
	@MockBean InventoryService inventoryService;
	
	@MockBean ItemRepository itemRepository;
	
	@Test
	void findingAllItems() {
		when(itemRepository.findAll()).thenReturn( // <1>
				Flux.just(new Item("item-1", "Alf alarm clock", //
						"nothing I really need", 19.99)));

		this.webTestClient.get().uri("/api/items") //
				.exchange() //
				.expectStatus().isOk() //
				.expectBody() //
				.consumeWith(document("findAll", preprocessResponse(prettyPrint()))); // <2>
	}
	
	@Test
	void postNewItem() {
		when(itemRepository.save(any())).thenReturn( //
				Mono.just(new Item("1", "Alf alarm clock", "nothing important", 19.99)));

		this.webTestClient.post().uri("/api/items") // <1>
				.bodyValue(new Item("Alf alarm clock", "nothing important", 19.99)) // <2>
				.exchange() //
				.expectStatus().isCreated() // <3>
				.expectBody() //
				.consumeWith(document("post-new-item", preprocessResponse(prettyPrint()))); // <4>
	}
}
