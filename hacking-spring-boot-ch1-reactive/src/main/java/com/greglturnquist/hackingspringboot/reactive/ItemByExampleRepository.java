package com.greglturnquist.hackingspringboot.reactive;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;

// Example 쿼리를 사용하기 위해 ReactiveQueryByExampleExecutor<T> 상속받기
public interface ItemByExampleRepository extends ReactiveQueryByExampleExecutor<Item> {

}
