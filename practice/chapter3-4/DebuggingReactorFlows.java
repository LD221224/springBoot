/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.greglturnquist.hackingspringboot.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class DebuggingReactorFlows {

	// 스레드 경계를 넘지 못하는 스택 트레이스
	static class SimpleExample {
		public static void main(String[] args) {
			ExecutorService executor = Executors.newSingleThreadScheduledExecutor();

			List<Integer> source;
			if (new Random().nextBoolean()) {
				source = IntStream.range(1, 11).boxed() //
						.collect(Collectors.toList());
			} else {
				source = Arrays.asList(1, 2, 3, 4);
			}

			try {
				executor.submit(() -> source.get(5)).get(); // line 52
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			} finally {
				executor.shutdown();
			}
		}
	}

	// 리액터로 작성한 코드
	static class ReactorExample {
		public static void main(String[] args) {
			Mono<Integer> source;
			if (new Random().nextBoolean()) {
				source = Flux.range(1, 10).elementAt(5);
			} else {
				source = Flux.just(1, 2, 3, 4).elementAt(5);
			}

			source //
					.subscribeOn(Schedulers.parallel()) //
					.block(); // line 74
		}
	}

	// Hooks.onOperatorDebug() 사용
	// 리액터가 처리 흐름 조립 시점에서의 호출부 세부정보를 수집하고 구독해서 실행되는 시점에 세부정보를 넘겨줌
	// 실제 운영환경에서느느 호출하면 안됨 (많은 비용 이슈)
	static class ReactorDebuggingExample {
		public static void main(String[] args) {

			Hooks.onOperatorDebug();

			Mono<Integer> source;
			if (new Random().nextBoolean()) {
				source = Flux.range(1, 10).elementAt(5);
			} else {
				source = Flux.just(1, 2, 3, 4).elementAt(5); // line 89
			}
			source //
					.subscribeOn(Schedulers.parallel()) //
					.block(); // line 93
		}
	}
	
}