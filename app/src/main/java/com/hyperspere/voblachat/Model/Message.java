package com.hyperspere.voblachat.Model;

public class Message {
	private String sender, chat;
	private String message;
	private String imagePath;

	private boolean viewed, delivered;
	private int type;

	public static final int MESSAGE_TYPE_TEXT = 1;
	public static final int MESSAGE_TYPE_PHOTO = 2;

	public Message(String sender, String chat, String message, boolean viewed, boolean delivered) {
		this.sender = sender;
		this.chat = chat;
		this.message = message;
		this.viewed = viewed;
		this.delivered = delivered;
		type = MESSAGE_TYPE_TEXT;
	}

	public boolean compare(Message msg){
		return msg.sender.equals(sender) &&
				msg.chat.equals(chat) &&
				msg.type == type &&
				(type == MESSAGE_TYPE_TEXT ? msg.message.equals(message) : msg.imagePath.equals(imagePath));
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Message() {
	}
}
