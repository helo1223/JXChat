//Szerveroldali osztály

package com.Helo.chat.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.Helo.chat.Controller;
import com.Helo.chat.UserMP;
import com.Helo.chat.net.packets.Packet;
import com.Helo.chat.net.packets.Packet.PacketTypes;
import com.Helo.chat.net.packets.Packet00Login;
import com.Helo.chat.net.packets.Packet01Disconnect;
import com.Helo.chat.net.packets.Packet02Message;
import com.vdurmont.emoji.EmojiParser;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class ChatServer extends Thread {
	private DatagramSocket socket;
	private Controller chat;
	public List<UserMP> connectedUsers = new ArrayList<UserMP>();

	public ChatServer(Controller chat) {
		this.chat = chat;
		try {
			//Megpróbáljuk lefoglalni az 1337-es portot
			this.socket = new DatagramSocket(1337);
		} catch (SocketException e) {
			System.err.println("Foglalt socket");
		}
	}

	public void run() {
		while (true) {
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
		}
	}

	private void parsePacket(byte[] data, InetAddress address, int port) {
		// Esetleges szóközök levágása
		String message = new String(data).trim();
		//A csomag elsõ két karakterébõl megállapítható, hogy milyen típusú (00 - LOGIN | 01 - DISCONNECT | 02 - MESSAGE) 
		PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
		Packet packet;
		switch (type) {
		default:
		case INVALID:
			break;
		case LOGIN:
			packet = new Packet00Login(data);
			System.out.println("[" + address.getHostAddress() + ":" + port + "]"
					+ ((Packet00Login) packet).getUserName() + " csatlakozott...");
			UserMP user = new UserMP(((Packet00Login) packet).getUserName(), address, port);
			this.addConnection(user, (Packet00Login) packet);
			try {

				String mess = ((Packet00Login) packet).getUserName() + " csatlakozott a szobához\n";
				String userName = ((Packet00Login) packet).getUserName();
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						chat.txt_chat.appendText(mess);
						chat.txt_chat.setStyle(chat.txt_chat.getText().indexOf(mess),(chat.txt_chat.getText().indexOf(mess)+mess.length()),Collections.singleton("green"));
						chat.txt_chat.moveTo(chat.txt_chat.getLength());
						chat.txt_chat.requestFollowCaret();
						chat.userslist.add(userName);
						chat.listProperty.set(FXCollections.observableArrayList(chat.userslist));
						if (!chat.muted) {
							playSound("appointed.mp3");
						}
					}
				});
				sendDataToAllClients(data);

			} catch (Exception e) {

			}
			break;
		case DISCONNECT:
			packet = new Packet01Disconnect(data);
			System.out.println("[" + address.getHostAddress() + ":" + port + "]"
					+ ((Packet01Disconnect) packet).getUserName() + " kilépett...");
			this.removeConnection((Packet01Disconnect) packet);
			try {
				String mess = ((Packet01Disconnect) packet).getUserName() + " kilépett a szobából\n";
				String userName = ((Packet01Disconnect) packet).getUserName();

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						chat.txt_chat.appendText(mess);
						chat.txt_chat.setStyle(chat.txt_chat.getText().indexOf(mess),(chat.txt_chat.getText().indexOf(mess)+mess.length()),Collections.singleton("red"));
						chat.txt_chat.moveTo(chat.txt_chat.getLength());
						chat.txt_chat.requestFollowCaret();
						chat.userslist.remove(userName);
						chat.listProperty.set(FXCollections.observableArrayList(chat.userslist));
						if (!chat.muted) {
							playSound("case-closed.mp3");
						}
					}
				});

			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case MESSAGE:
			packet = new Packet02Message(data);
			System.out.println(
					"[" + address.getHostAddress() + ":" + port + "]" + ((Packet02Message) packet).getUserName()
							+ " Üzenete: " + ((Packet02Message) packet).getMessage());
			try {
				String myString = ((Packet02Message) packet).getMessage();
				String result1 = EmojiParser.parseToUnicode(myString);
				String result2 = Emoji.replaceInText(result1);
				String userName = ((Packet02Message) packet).getUserName();
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						String result = userName + ": "+result2+"\n";
						chat.txt_chat.appendText(result);
						chat.txt_chat.clearStyle(chat.txt_chat.getText().indexOf(result),(chat.txt_chat.getText().indexOf(result)+result.length()));
						chat.txt_chat.moveTo(chat.txt_chat.getLength());
						chat.txt_chat.requestFollowCaret();
						if (!userName.equals(chat.user.getUsername())) {
								if (!chat.muted) {
										playSound("intuition.mp3");
									}
							}
						}
			
					});
			}catch(Exception e) {
				e.printStackTrace();
			}
			sendDataToAllClients(data);

			break;
		}

	}

	public void addConnection(UserMP user, Packet00Login packet) {
		boolean alreadyConnected = false;
		for (UserMP p : this.connectedUsers) {
			if (user.getUsername().equalsIgnoreCase(p.getUsername())) {
				if (p.ipAddress == null) {
					p.ipAddress = user.ipAddress;
				}

				if (p.port == -1) {
					p.port = user.port;
				}
				alreadyConnected = true;
			} else {
				sendData(packet.getData(), p.ipAddress, p.port);
				packet = new Packet00Login(p.getUsername());
				sendData(packet.getData(), user.ipAddress, user.port);
			}
		}
		if (!alreadyConnected) {
			this.connectedUsers.add(user);
		}
	}

	public void removeConnection(Packet01Disconnect packet) {
		this.connectedUsers.remove(getUserMPIndex(packet.getUserName()));
		packet.writeData(this);
	}

	public UserMP getUserMP(String username) {
		for (UserMP user : this.connectedUsers) {
			if (user.getUsername().equals(username)) {
				return user;
			}
		}
		return null;
	}

	public int getUserMPIndex(String username) {
		int index = 0;
		for (UserMP user : this.connectedUsers) {
			if (user.getUsername().equals(username)) {
				break;
			}
			index++;
		}
		return index;
	}

	public void sendData(byte[] data, InetAddress ipAddress, int port) {
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
		try {
			this.socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendDataToAllClients(byte[] data) {
		for (UserMP p : connectedUsers) {
			sendData(data, p.ipAddress, p.port);
		}

	}
	
	public void removeAll() {
		for (UserMP user: this.connectedUsers) {
			this.connectedUsers.remove(user);
			
		}
	}
	

	private void playSound(String file) {
		URL fileUrl = ChatServer.class.getResource(file);
		Media hit = new Media(fileUrl.toString());
		MediaPlayer mediaPlayer = new MediaPlayer(hit);
		mediaPlayer.play();
	}
	
	public void closeServer() {
		this.socket.close();
		this.interrupt();
	}
}
