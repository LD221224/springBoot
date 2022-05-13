package com.greglturnquist.hackingspringboot.reactive;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.*;

@Disabled("pom.xml에서 blockhound-junit-platform 의존 관계를 제거한 후에 실행해야 성공한다.")
// @SpringBootTest : 스프링 부트가 실제 애플리케이션을 구동
// @SpringBootApplication이 붙은 클래스를 찾아서 내장 컨테이너를 실행한다.
// WebEnvironment.RANDOM_PORT : 테스트할 때 임의의 포트에 내장 컨테이너를 바인딩
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
// @AutoConfigureWebClient : 애플리케이션에 요청을 날리는 WebTestClient 인스턴스를 생성
@AutoConfigureWebClient
public class LoadingWebSiteIntegrationTest {
	// @Autowired를 사용해 WebTestClient 인스턴스를 테스트 케이스에 주입
	@Autowired WebTestClient client;
	
	// 실제 테스트 메소드는 WebTestClient를 사용해서 홈 컨트롤러의 루트 경로를 호출
	// WebTestClient에는 단언 기능이 포함되어 있음
	// HTTP 응답 코드, Content-Type 헤더를 검증하고, 
	// Consumer를 사용해 응답 본문(response body)에 값 검증을 수행
	@Test
	void test() {
		client.get().uri("/").exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(TEXT_HTML)
		.expectBody(String.class)
		.consumeWith(exchangeResult -> {
			assertThat(exchangeResult.getResponseBody()).contains("<a href=\"/add");
		});
	}

}
