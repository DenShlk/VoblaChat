package com.hyperspere.voblachat.Model;

public class User {
	private String id, username;

	public User(String id, String username) {
		this.id = id;
		this.username = username;
	}

	public User() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
