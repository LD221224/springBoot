package com.greglturnquist.hackingspringboot.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.thymeleaf.TemplateEngine;

//import reactor.blockhound.BlockHound;

@SpringBootApplication
public class HackingSpringBootApplication {

	public static void main(String[] args) {
		
		// 블록하운드 등록
		// TemplateEngine.process()를 블록하운드 허용 리스트에 추가
//		BlockHound.builder()
//		.allowBlockingCallsInside(
//				TemplateEngine.class.getCanonicalName(), "process")
//		.install();
		
		SpringApplication.run(HackingSpringBootApplication.class, args);
	}

}
