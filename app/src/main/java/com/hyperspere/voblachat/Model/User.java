package com.hyperspere.voblachat.Model;

import java.util.List;

public class User {
	private String id, username;
	private List<String> myChats;

	public User(String id, String username, List<String> myChats) {
		this.id = id;
		this.username = username;
		this.myChats = myChats;
	}

	public User() {
	}

	public List<String> getMyChats() {
		return myChats;
	}

	public void setMyChats(List<String> myChats) {
		this.myChats = myChats;
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
