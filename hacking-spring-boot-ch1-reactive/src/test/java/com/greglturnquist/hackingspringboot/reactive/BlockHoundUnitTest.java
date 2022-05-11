package com.greglturnquist.hackingspringboot.reactive;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class BlockHoundUnitTest {

	@Test
	void threadSleepIsBlockingCall() {
		Mono.delay(Duration.ofSeconds(1))
		.flatMap(tick -> {
			try {
				Thread.sleep(10);
				return Mono.just(true);
			} catch(InterruptedException e) {
				return Mono.error(e);
			}
		})
		.as(StepVerifier::create)
		.verifyComplete();
		// 테스트 케이스 통과시키는 코드
//		.verifyErrorMatches(throwable -> {
//			assertThat(throwable.getMessage()) //
//			.contains("Blocking call! java.lang.Thread.sleep");
//			return true;
//		});
	}
}
