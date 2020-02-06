package com.hyperspere.voblachat.Model;

public class Message {
	private String sender, chat;
	private String message;
	private boolean viewed, delivered;

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getChat() {
		return chat;
	}

	public void setChat(String chat) {
		this.chat = chat;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isViewed() {
		return viewed;
	}

	public void setViewed(boolean viewed) {
		this.viewed = viewed;
	}

	public boolean isDelivered() {
		return delivered;
	}

	public void setDelivered(boolean delivered) {
		this.delivered = delivered;
	}

	public Message(String sender, String chat, String message, boolean viewed, boolean delivered) {
		this.sender = sender;
		this.chat = chat;
		this.message = message;
		this.viewed = viewed;
		this.delivered = delivered;
	}

	public Message() {
	}
}
