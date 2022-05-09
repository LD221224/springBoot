package com.greglturnquist.hackingspringboot.reactive;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;

class Cart {
	
	private @Id String id;
	private List<CartItem> cartItems;
	
	private Cart() {
		
	}
	
	public Cart(String id) {
		this(id, new ArrayList<>());
	}
	
	public Cart(String id, List<CartItem> cartItems) {
		this.id = id;
		this.cartItems = cartItems;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<CartItem> getCartItems() {
		return cartItems;
	}

	public void setCartItems(List<CartItem> cartItems) {
		this.cartItems = cartItems;
	}

	@Override
	public int hashCode() {
		return Objects.hash(cartItems, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cart other = (Cart) obj;
		return Objects.equals(cartItems, other.cartItems) && Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "Cart [id=" + id + ", cartItems=" + cartItems + "]";
	}
}
