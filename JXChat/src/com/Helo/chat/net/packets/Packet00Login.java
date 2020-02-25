package com.Helo.chat.net.packets;

import com.Helo.chat.net.ChatClient;
import com.Helo.chat.net.ChatServer;

public class Packet00Login extends Packet {

	private String userName;

	public Packet00Login(byte[] data) {
		super(00);
		String[] dataArray = readData(data).split(",");
		this.userName = dataArray[0];
	}

	public Packet00Login(String userName) {
		super(00);
		this.userName = userName;
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
		return ("00" + this.userName).getBytes();
	}

	public String getUserName() {
		return userName;
	}

}
