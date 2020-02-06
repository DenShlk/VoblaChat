package com.hyperspere.voblachat.Model;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Chat {
	private String id;
	private String name;

	private List<String> members;
	private List<String> messages;

	public Chat(String id, String name, List<String> members, List<String> messages) {
		this.id = id;
		this.name = name;
		this.members = members;
		this.messages = messages;
	}

	public Chat(DataSnapshot snapshot) {
		id = snapshot.getKey();
		name = snapshot.child("Name").getValue(String.class);
		members = new ArrayList<>();
		DataSnapshot membersSnapshot = snapshot.child("Members");
		for(DataSnapshot memberId : membersSnapshot.getChildren()){
			members.add(memberId.getValue(String.class));
		}
	}

	public Chat() {
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

	public List<String> getMembers() {
		return members;
	}

	public void setMembers(List<String> members) {
		this.members = members;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
}
