class SimpleServer {
	private final KitchenService kitchen;
	
	SimpleServer(KitchenService kitchen){
		this.kitchen = kitchen;
	}
	
	Flux<Dish> doingMyJob(){
		return this.kitchen.getDishes()
				.map(dish -> Dish.deliver(dish));
		
	}
}
