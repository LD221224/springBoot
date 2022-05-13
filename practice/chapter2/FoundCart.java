public class FoundCart {
//	전통적인 반복문을 활용한 장바구니 탐색
	boolean found = false;
	
	for(CartItem cartItem : cart.getCartItems()) {
		if(cartItem.getItem().getId().equals("5")) {
			found = true;
		}
	}
	
	if(found) {
//		수량증가
	}else {
//		새 구매 상품(CartItem) 항목 추가
	}
	
//	스트림 API를 활용한 장바구니 탐색
	if(cart.getCartItems().stream()
			.anyMatch(cartItem -> cartItem.getItem().getId().equals("5"))) {
//		수량증가
	}else {
//		새 구매 상품(CartItem) 항목 추가
	}
}
