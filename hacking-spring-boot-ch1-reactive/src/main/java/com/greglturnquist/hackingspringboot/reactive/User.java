package com.greglturnquist.hackingspringboot.reactive;

import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;

public class User {

	private @Id String id;
	private String name;
	private String password;
	private List<String> roles;
	
	private User() {}
	
	public User(String id, String name, String password, List<String> roles) {
		this.id = id;
		this.name = name;
		this.password = password;
		this.roles = roles;
	}
	
	public User(String name, String password, List<String> roles) {
		this.name = name;
		this.password = password;
		this.roles = roles;		
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, password, roles);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return Objects.equals(id, other.id) && Objects.equals(name, other.name)
				&& Objects.equals(password, other.password) && Objects.equals(roles, other.roles);
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", password=" + password + ", roles=" + roles + "]";
	}
}
