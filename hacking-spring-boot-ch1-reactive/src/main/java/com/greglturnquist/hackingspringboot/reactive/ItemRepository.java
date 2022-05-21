package com.greglturnquist.hackingspringboot.reactive;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

// 판매 상품(item)용 리액티브 데이터 리포지토리
// 저장, 조회, 삭제 같은 단순하고 공통적인 연산을 추상화 -> 리포지토리
public interface ItemRepository extends ReactiveCrudRepository<Item, String>, 
ReactiveQueryByExampleExecutor<Item> {
	// 검색어로 상품 목록을 조회하는 리포지토리
	Flux<Item> findByNameContaining(String partialName);

//	// 직접 작성한 쿼리문을 사용
//	@Query("{'name' : ?0, 'age' : ?1}")
//	Flux<Item> findItemsForCustomerMonthlyReport(String name, int age);
//
//	@Query(sort = "{'age' : -1}")
//	Flux<Item> findSortedStuffForWeeklyReport();

	// search by name
	Flux<Item> findByNameContainingIgnoreCase(String partialName);

	// search by description
	Flux<Item> findByDescriptionContainingIgnoreCase(String partialName);

	// search by name AND description
	Flux<Item> findByNameContainingAndDescriptionContainingAllIgnoreCase(String partialName, String partialDesc);

	// search by name OR description
	Flux<Item> findByNameContainingOrDescriptionContainingAllIgnoreCase(String partialName, String partialDesc);

	Mono<Item> findByName(String name);
}
