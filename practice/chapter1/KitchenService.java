class KitchenService {
//	�ڹ� ���׸� : Ŭ���� �ܺο��� ����ڿ� ���� �������� Ÿ���� ����
	Flux<Dish> getDishes(){
		return Flux.just(
				new Dish("Sesame chicken"),
				new Dish("Lo mein noodles, plain"),
				new Dish("Sweet & sour beef"));
	}
}
