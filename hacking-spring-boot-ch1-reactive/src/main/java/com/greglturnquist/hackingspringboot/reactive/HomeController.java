package com.greglturnquist.hackingspringboot.reactive;

// Mono는 0 또는 1개의 원소만 담을 수 있는 리액티브 발행자(publisher)
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// JSON이나 XML 같은 데이터가 아닌 템플릿을 사용한 웹 페이지를 반환하는 스프링 웹 컨트롤러
@Controller
public class HomeController {

//	GET 요청을 처리함, 기본값 '/'
	@GetMapping
	Mono<String> home(){
		return Mono.just("home");
	}
}
