package com.Helo.chat.net.packets;

import com.Helo.chat.net.ChatClient;
import com.Helo.chat.net.ChatServer;

public class Packet01Disconnect extends Packet {

	private String userName;

	public Packet01Disconnect(byte[] data) {
		super(01);
		this.userName = readData(data);
	}

	public Packet01Disconnect(String userName) {
		super(01);
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
		return ("01" + this.userName).getBytes();
	}

	public String getUserName() {
		return userName;
	}

}
