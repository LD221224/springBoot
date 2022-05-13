class PoliteRestaurant {
	
	public static void main(String... args) {
		PoliteServer server = new PoliteServer(new KitchenService());
		
		server.doingMyjob().subscribe(
				dish -> System.out.println("Consuming" + dish),
				throwable -> System.err.println(throwable));
			
	}
}
