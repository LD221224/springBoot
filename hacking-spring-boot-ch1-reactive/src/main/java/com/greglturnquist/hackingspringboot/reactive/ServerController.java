package com.greglturnquist.hackingspringboot.reactive;

// Flux<T>는 실제 물건을 전달해주는 역할로 레스토랑의 서빙 직원과 비슷하다.
// 주방에서 요리가 완성되면 주방에서 요리를 받아 손님에게 가져다주고, 
// 다시 제자리로 돌아와 다음 요리를 기다린다.
import reactor.core.publisher.Flux;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//화면 구성을 위한 템플릿을 사용하는 대신에 결과 데이터를 직렬화하고
//HTTP 응답 본문에 직접 써서 반환하는 REST 컨트롤러
@RestController
public class ServerController {
	
	private final KitchenService kitchen;
	
	public ServerController(KitchenService kitchen) {
//		애플리케이션 실행 시 스프링은 KitchenService의 인스턴스를 찾아 자동으로 생성자에 주입해줌
		this.kitchen = kitchen;
	}
	
	@GetMapping(value = "/server", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//	/server로 향하는 HTTP GET 요청을 serverDishes 메소드로 라우팅해주는 스프링 웹 MVC 애너테이션
	Flux<Dish> serveDishes() {
		return this.kitchen.getDishes();
	}
	
//	요리를 전달하는 함수
	@GetMapping(value = "/served-dishes", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<Dish> deliverDishes() {
		return this.kitchen.getDishes()
				.map(dish -> Dish.deliver(dish));
	}
}
