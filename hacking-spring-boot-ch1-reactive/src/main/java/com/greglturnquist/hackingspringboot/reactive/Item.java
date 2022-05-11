package com.greglturnquist.hackingspringboot.reactive;

import java.util.Date;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

public class Item {
	
	private @Id String id;
	private String name;
	private String description;
	private double price;
	private String distributorRegion;
	private Date releaseDate;
	private int availableUnits;
	private Point location;
	private boolean active;
	
	private Item() {
		
	}
	
	Item(String name, String description, double price) {
		this.name = name;
		this.description = description;
		this.price = price;
	}
	
    Item(String id, String name, String description, double price) {
        this(name, description, price);
        this.id = id;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDistributorRegion() {
		return distributorRegion;
	}

	public void setDistributorRegion(String distributorRegion) {
		this.distributorRegion = distributorRegion;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public int getAvailableUnits() {
		return availableUnits;
	}

	public void setAvailableUnits(int availableUnits) {
		this.availableUnits = availableUnits;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Item other = (Item) obj;
//		return active == other.active && availableUnits == other.availableUnits
//				&& Objects.equals(description, other.description)
//				&& Objects.equals(distributorRegion, other.distributorRegion) && Objects.equals(id, other.id)
//				&& Objects.equals(location, other.location) && Objects.equals(name, other.name)
//				&& Double.doubleToLongBits(price) == Double.doubleToLongBits(other.price)
//				&& Objects.equals(releaseDate, other.releaseDate);
//	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Item item = (Item) o;
        return Double.compare(item.price, price) == 0 &&
            Objects.equals(id, item.id) &&
            Objects.equals(name, item.name) &&
            Objects.equals(description, item.description);
    }

	@Override
	public int hashCode() {
		return Objects.hash(active, availableUnits, description, distributorRegion, id, location, name, price,
				releaseDate);
	}

//	@Override
//	public String toString() {
//		return "Item [id=" + id + ", name=" + name + ", description=" + description + ", price=" + price
//				+ ", distributorRegion=" + distributorRegion + ", releaseDate=" + releaseDate + ", availableUnits="
//				+ availableUnits + ", location=" + location + ", active=" + active + "]";
//	}
    @Override
    public String toString() {
        return "Item{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", price=" + price +
            '}';
    }
}
