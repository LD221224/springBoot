package com.greglturnquist.hackingspringboot.reactive;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.*;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.web.reactive.server.WebTestClient;

// 행동 유도성 기반 문서화 테스트 클래스
@WebFluxTest(controllers = AffordancesItemController.class) 
@AutoConfigureRestDocs 
public class AffordancesItemControllerDocumentationTest {

	@Autowired private WebTestClient webTestClient; 

	@MockBean InventoryService service; 
	
	@MockBean ItemRepository repository; 

	// 스프링 레스트 독을 활용한 행동 유도성 테스트
	@Test
	void findSingleItemAffordances() {
		when(repository.findById("item-1")).thenReturn(Mono.just( 
				new Item("item-1", "Alf alarm clock", "nothing I really need", 19.99)));

		this.webTestClient.get().uri("/affordances/items/item-1") 
				.accept(MediaTypes.HAL_FORMS_JSON) 
				.exchange() 
				.expectStatus().isOk() 
				.expectBody() 
				.consumeWith(document("single-item-affordances", 
						preprocessResponse(prettyPrint()))); 
	}

	@Test
	void findAggregateRootItemAffordances() {
		when(repository.findAll()).thenReturn(Flux.just( 
				new Item("Alf alarm clock", "nothing I really need", 19.99)));
		when(repository.findById((String) null)).thenReturn(Mono.just( 
				new Item("item-1", "Alf alarm clock", "nothing I really need", 19.99)));

		this.webTestClient.get().uri("/affordances/items") 
				.accept(MediaTypes.HAL_FORMS_JSON) 
				.exchange() 
				.expectStatus().isOk() 
				.expectBody() 
				.consumeWith(document("aggregate-root-affordances", preprocessResponse(prettyPrint()))); 
	}

}