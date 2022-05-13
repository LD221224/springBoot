class KitchenService {
//	자바 제네릭 : 클래스 외부에서 사용자에 의해 데이터의 타입을 지정
	Flux<Dish> getDishes(){
		return Flux.just(
				new Dish("Sesame chicken"),
				new Dish("Lo mein noodles, plain"),
				new Dish("Sweet & sour beef"));
	}
}
