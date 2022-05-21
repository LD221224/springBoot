package com.greglturnquist.hackingspringboot.reactive;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
public class HomeControllerTest {

	@Autowired WebTestClient webTestClient;
	@Autowired ItemRepository repository;

// POST 요청 테스트
	@Test
	// 테스트용 가짜 사용자 alice
	@WithMockUser(username = "alice", roles = { "SOME_OTHER_ROLE" }) 
	void addingInventoryWithoutProperRoleFails() {
		this.webTestClient.post().uri("/") 
				.exchange() 
				// HTTP 403 Forbidden 상태 코드가 반환되는지 확인
				.expectStatus().isForbidden();
	}

	@Test
	@WithMockUser(username = "bob", roles = { "INVENTORY" }) 
	void addingInventoryWithProperRoleSucceeds() {
		this.webTestClient 
				.post().uri("/") 
				.contentType(MediaType.APPLICATION_JSON) 
				.bodyValue("{" + 
						"\"name\": \"iPhone 11\", " + 
						"\"description\": \"upgrade\", " + 
						"\"price\": 999.99" + 
						"}") 
				.exchange() 
				.expectStatus().isOk(); 

		this.repository.findByName("iPhone 11")
				.as(StepVerifier::create) 
				.expectNextMatches(item -> { 
					assertThat(item.getDescription()).isEqualTo("upgrade");
					assertThat(item.getPrice()).isEqualTo(999.99);
					return true; 
				}) 
				.verifyComplete(); 
	}

	
// DELETE 요청 테스트
	@Test
	@WithMockUser(username = "carol", roles = { "SOME_OTHER_ROLE" })
	void deletingInventoryWithoutProperRoleFails() {
		this.webTestClient.delete().uri("/some-item") 
				.exchange() 
				.expectStatus().isForbidden();
	}

	@Test
	@WithMockUser(username = "dan", roles = { "INVENTORY" })
	void deletingInventoryWithProperRoleSucceeds() {
		String id = this.repository.findByName("Alf alarm clock") 
				.map(Item::getId) 
				.block();

		this.webTestClient 
				.delete().uri("/" + id) 
				.exchange() 
				.expectStatus().isOk();

		this.repository.findByName("Alf alarm clock") 
				.as(StepVerifier::create) 
				.expectNextCount(0) 
				.verifyComplete();
	}
}