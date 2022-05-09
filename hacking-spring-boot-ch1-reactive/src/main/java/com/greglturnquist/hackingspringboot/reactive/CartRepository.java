package com.greglturnquist.hackingspringboot.reactive;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

// Cart 객체 관리를 위한 리액티브 리포지토리
public interface CartRepository extends ReactiveCrudRepository<Cart, String>{

}
