package com.Helo.chat.net.packets;

import com.Helo.chat.net.ChatClient;
import com.Helo.chat.net.ChatServer;

public class Packet02Message extends Packet {

	private String userName;
	private String message;

	public Packet02Message(byte[] data) {
		super(02);
		String[] dataArray = readData(data).split("/;");
		this.userName = dataArray[0];
		this.message = dataArray[1];

	}

	public Packet02Message(String userName, String message) {
		super(02);
		this.userName = userName;
		this.message = message;
	}

	@Override
	public void writeData(ChatClient client) {
		client.sendData(getData());
	}

	@Override
	public void writeData(ChatServer server) {
		server.sendDataToAllClients(getData());

	}

	@Override
	public byte[] getData() {
		return ("02" + this.userName + "/;" + this.message).getBytes();
	}

	public String getUserName() {
		return userName;
	}

	public String getMessage() {
		return message;
	}

}


//Üzenetküldéskor küldött csomag
//Felépítése: 02Felhasználónév;üzenet
