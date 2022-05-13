class PoliteServer {
	private final KitchenService kitchen;
	
	PoliteServer(KitchenService kitchen){
		this.kitchen = kitchen;
	}
	
	Flux<Dish> doingMyJob(){
		return this.kitchen.getDishes()
				.doOnNext(dish -> System.out.println("Thank you for" + dish + "!"))
				.doOnError(error -> System.out.println("So sorry about" + error.getMessage()))
				.doOnComplate(() -> System.out.println("Thanks for all your hard work!"))
				.map(Dish::deliver);
	}
}
