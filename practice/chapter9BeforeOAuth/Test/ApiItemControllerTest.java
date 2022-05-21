package com.greglturnquist.hackingspringboot.reactive;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.*;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.HypermediaWebTestClientConfigurer;
import reactor.test.StepVerifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.WebClientConfigurer;
import org.springframework.hateoas.server.core.TypeReferences.CollectionModelType;
import org.springframework.hateoas.server.core.TypeReferences.EntityModelType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest()
@EnableHypermediaSupport(type = HAL) // <1>
@AutoConfigureWebTestClient
public class ApiItemControllerTest {

	@Autowired WebTestClient webTestClient; // <2>

	@Autowired ItemRepository repository;

	@Autowired HypermediaWebTestClientConfigurer webClientConfigurer; // <3>

	@BeforeEach
	void setUp() {
		this.webTestClient = this.webTestClient.mutateWith(webClientConfigurer); // <4>
	}
	
	// 비인가 사용자 테스트
	@Test
	@WithMockUser(username = "alice", roles = { "SOME_OTHER_ROLE" }) 
	void addingInventoryWithoutProperRoleFails() {
		this.webTestClient 
				.post().uri("/api/items/add") 
				.contentType(MediaType.APPLICATION_JSON) 
				.bodyValue("{" + //
						"\"name\": \"iPhone X\", " + 
						"\"description\": \"upgrade\", " + 
						"\"price\": 999.99" + 
						"}") 
				.exchange() 
				.expectStatus().isForbidden(); 
	}

	// 인가된 사용자 테스트
	@Test
	@WithMockUser(username = "bob", roles = { "INVENTORY" }) 
	void addingInventoryWithProperRoleSucceeds() {
		this.webTestClient 
				.post().uri("/api/items/add") 
				.contentType(MediaType.APPLICATION_JSON) 
				.bodyValue("{" + 
						"\"name\": \"iPhone X\", " + 
						"\"description\": \"upgrade\", " + 
						"\"price\": 999.99" + 
						"}") 
				.exchange() 
				.expectStatus().isCreated(); 

		this.repository.findByName("iPhone X") 
				.as(StepVerifier::create) 
				.expectNextMatches(item -> { 
					assertThat(item.getDescription()).isEqualTo("upgrade");
					assertThat(item.getPrice()).isEqualTo(999.99);
					return true; 
				}) 
				.verifyComplete(); 
	}
	
	// 하이퍼미디어 테스트
	@Test
	@WithMockUser(username = "alice", roles = { "INVENTORY" })
	void navigateToItemWithInventoryAuthority() {

		// Navigate to the root URI of the API.
		RepresentationModel<?> root = this.webTestClient.get().uri("/api") //
				.exchange() //
				.expectBody(RepresentationModel.class) //
				.returnResult().getResponseBody();

		// Drill down to the Item aggregate root.
		CollectionModel<EntityModel<Item>> items = this.webTestClient.get() //
				.uri(root.getRequiredLink(IanaLinkRelations.ITEM).toUri()) //
				.exchange() //
				.expectBody(new CollectionModelType<EntityModel<Item>>() {}) //
				.returnResult().getResponseBody();

		assertThat(items.getLinks()).hasSize(2);
		assertThat(items.hasLink(IanaLinkRelations.SELF)).isTrue();
		assertThat(items.hasLink("add")).isTrue();

		// Find the first Item...
		EntityModel<Item> first = items.getContent().iterator().next();

		// ...and extract it's single-item entry.
		EntityModel<Item> item = this.webTestClient.get() //
				.uri(first.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
				.exchange() //
				.expectBody(new EntityModelType<Item>() {}) //
				.returnResult().getResponseBody();

		assertThat(item.getLinks()).hasSize(3);
		assertThat(item.hasLink(IanaLinkRelations.SELF)).isTrue();
		assertThat(item.hasLink(IanaLinkRelations.ITEM)).isTrue();
		assertThat(item.hasLink("delete")).isTrue();
	}
}