package com.greglturnquist.hackingspringboot.reactive;

import java.util.Objects;

class CartItem {
	private Item item;
	private int quantity;
	
	private CartItem() {
		
	}
	
	CartItem(Item item) {
		this.item = item;
		this.quantity = 1;
	}

	public void increment() {
		this.quantity++;
	}
	
	public void decrement() {
		this.quantity--;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@Override
	public int hashCode() {
		return Objects.hash(item, quantity);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CartItem other = (CartItem) obj;
		return Objects.equals(item, other.item) && quantity == other.quantity;
	}

	@Override
	public String toString() {
		return "CartItem [item=" + item + ", quantity=" + quantity + "]";
	}

}
