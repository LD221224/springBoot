package com.greglturnquist.hackingspringboot.reactive;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class KitchenService {
	// 요리 스트림 생성
	Flux<Dish> getDishes() {
		// 세 가지 요리 중 무작위로 선택된 1개의 요리를 250밀리초 간격으로 계속 제공
		return Flux.<Dish> generate(sink -> sink.next(randomDish()))
				.delayElements(Duration.ofMillis(250));
	}

	// 요리 무작위 선택
	private Dish randomDish() {
		return menu.get(picker.nextInt(menu.size()));
	}

	private List<Dish> menu = Arrays.asList(
			new Dish("Sesame chicken"),
			new Dish("Lo mein noodles, plain"),
			new Dish("Sweet & sour beef"));

	private Random picker = new Random();
}
