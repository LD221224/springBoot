static class ReactorDebuggingExample {
	public static void main(String[] args) {
		
		Hooks.onOperatorDebug();
		
		Mono<Integer> source;
		if(new Random.nextBoolean()) {
			source = Flux.range(1, 10).elementAt(5);
		}else {
			source = Flux.just(1, 2, 3, 4).elementAt(5)l
		}
		
		source
		.subscribeOn(Schedulers.parallel())
		.block();
	}
}
