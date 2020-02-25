package com.Helo.chat;

import com.Helo.chat.net.ChatServer;

public class User {
	public ChatServer socketClient;
	String userName;
	public Chat chat;

	public User(String userName) {
		this.userName = userName;
	}

	public String getUsername() {
		return this.userName;
	}
}
