package com.Helo.chat;

import java.net.InetAddress;

public class UserMP extends User {

	public InetAddress ipAddress;
	public int port;

	public UserMP(String userName, InetAddress ipAddress, int port) {
		super(userName);
		this.ipAddress = ipAddress;
		this.port = port;
	}

}
