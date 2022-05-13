import org.springframework.data.repository.CrudRepository;

// 블로킹 리포지토리
// Mono나 Flux를 반환하지 않는 메소드를 포함하는 인터페이스
// 결과를 받을 때까지 기다렸다가 응답을 반환하는 전통적인 블로킹 API
interface BlockingItemRepository extends CrudRepository<Item, String>{

}
