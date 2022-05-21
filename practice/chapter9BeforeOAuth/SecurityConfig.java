package com.greglturnquist.hackingspringboot.reactive;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
//메소드 수준 보안 활성화
@EnableReactiveMethodSecurity
public class SecurityConfig {

	@Bean
	public ReactiveUserDetailsService userDetailsService(UserRespository respository) {
		return username -> respository.findByName(username)
				// 개발자가 정의한 User 객체를 조회했으면 map()을 황용해
				// 스프링 시큐리티의 UserDetails 객체로 변환한다.
				.map(user -> User.withDefaultPasswordEncoder()
						.username(user.getName())
						.password(user.getPassword())
						.authorities(user.getRoles().toArray(new String[0]))
						// 평문형 API의 build() 메소드를 사용해 UserDetails 객체를 만들어낸다.
						.build());
	}
	
	// 커스텀 정책 작성
	static final String USER = "USER";
	static final String INVENTORY = "INVENTORY";
	
	@Bean
	SecurityWebFilterChain myCustomSecurityPolicy(ServerHttpSecurity http) {
		return http
				.authorizeExchange(exchanges -> exchanges
						// authorization 규칙
						// HTTP 동사, URL 패턴, 역할 등 접근 제어에 사용할 모든 규칙을 정의한다.
						// /로 들어오는 POST 요청, **/로 들어오는 DELETE 요청이 ROLE_INVENTORY라는 역할을 가진
						// 사용자로부터 전송되었을 때만 진입을 허용한다.
						.pathMatchers(HttpMethod.POST, "/").hasRole(INVENTORY)
						.pathMatchers(HttpMethod.DELETE, "/**").hasRole(INVENTORY)
						.anyExchange().authenticated()
						.and()
						.httpBasic()
						.and()
						.formLogin())
				.csrf().disable()
				.build();
	}
	
	// 각기 역할이 다른 테스트용 사용자 추가
	static String role(String auth) {
		return "ROLE_" + auth;
	}
	
	// 테스트용 사용자 추가
	@Bean
	CommandLineRunner userLoader(MongoOperations operations) {
		return args -> {
			operations.save(new com.greglturnquist.hackingspringboot.reactive.User(
					"ld", "password", Arrays.asList(role(USER))));
		
			operations.save(new com.greglturnquist.hackingspringboot.reactive.User(
					"manager", "password", Arrays.asList(role(USER), role(INVENTORY))));
		};
	}
	
}
