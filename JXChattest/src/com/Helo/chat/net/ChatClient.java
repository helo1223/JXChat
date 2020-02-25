//Kliensoldali oszt�ly

package com.Helo.chat.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;

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

public class ChatClient extends Thread {

	private InetAddress ipAddress;
	private DatagramSocket socket;
	private Controller chat;

	public ChatClient(Controller chat, String ipAddress) {
		this.chat = chat;
		try {
			this.socket = new DatagramSocket();
			this.ipAddress = InetAddress.getByName(ipAddress);
		} catch (SocketException e) {
			System.err.println("Socket Hiba");
		} catch (UnknownHostException e) {
			System.err.println("Ismeretlen Host Hiba");
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
				socket.disconnect();

			}
			this.parsePacket(packet.getData(), packet.getAddress(), packet.getPort());

			if (packet.equals(null)) {
			}
		}
	}

	// K�l�nf�le csomagt�pusok feldolgoz�sa
	private void parsePacket(byte[] data, InetAddress address, int port) {
		// Esetleges sz�k�z�k lev�g�sa
		String message = new String(data).trim();
		// A csomag els� k�t karakter�b�l meg�llap�that�, hogy milyen t�pus� (00 - LOGIN
		// | 01 - DISCONNECT | 02 - MESSAGE)
		PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
		Packet packet;
		switch (type) {
		default:
		case INVALID:
			break;
		case LOGIN:
			packet = new Packet00Login(data);
			handleLogin((Packet00Login) packet, address, port);

			try {

				String mess = ((Packet00Login) packet).getUserName() + " csatlakozott a szob�hoz\n";
				String userName = ((Packet00Login) packet).getUserName();

				// Ez a r�sz az�rt sz�ks�ges, mert a JavaFX grafikus fel�let�nek friss�t�s�t
				// k�l�n sz�lon kell futtatni, k�l�nben �sszeomlik
				// Ez azt eredm�nyezn�, hogy a sz�vegben minden karakter ut�n sort�r�s lenne
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (!chat.userslist.contains(userName)) {
							chat.txt_chat.appendText(mess);
							// Ha valaki bel�p a szob�ba, a sor elej�t�l a v�g�ig z�ld h�ttere legyen az
							// �zenetnek
							chat.txt_chat.setStyle(chat.txt_chat.getText().indexOf(mess),
									(chat.txt_chat.getText().indexOf(mess) + mess.length()),
									Collections.singleton("green"));
							chat.txt_chat.moveTo(chat.txt_chat.getLength());
							chat.txt_chat.requestFollowCaret();
							chat.userslist.add(userName);

						}
						chat.listProperty.set(FXCollections.observableArrayList(chat.userslist));
						if (!chat.muted) {
							playSound("appointed.mp3");
						}
					}
				});

			} catch (Exception e) {

			}
			break;
		case DISCONNECT:
			packet = new Packet01Disconnect(data);
			System.out.println("[" + address.getHostAddress() + ":" + port + "]"
					+ ((Packet01Disconnect) packet).getUserName() + " kil�pett...");
			try {
				String mess = ((Packet01Disconnect) packet).getUserName() + " kil�pett a szob�b�l\n";
				String userName = ((Packet01Disconnect) packet).getUserName();
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						chat.txt_chat.appendText(mess);
						// Ha valaki kil�p a szob�b�l, a sor elej�t�l a v�g�ig piros h�ttere legyen az
						// �zenetnek
						chat.txt_chat.setStyle(chat.txt_chat.getText().indexOf(mess),
								(chat.txt_chat.getText().indexOf(mess) + mess.length()), Collections.singleton("red"));
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		case MESSAGE:
			packet = new Packet02Message(data);
			try {
				// Feleslegesen sok bajl�d�s az unicode emoji-k t�mogat�s��rt
				String myString = ((Packet02Message) packet).getMessage();
				String result1 = EmojiParser.parseToUnicode(myString);
				String result2 = Emoji.replaceInText(result1);
				String userName = ((Packet02Message) packet).getUserName();
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						String result = userName + ": " + result2 + "\n";
						chat.txt_chat.appendText(result);
						chat.txt_chat.clearStyle(chat.txt_chat.getText().indexOf(result),
								(chat.txt_chat.getText().indexOf(result) + result.length()));
						chat.txt_chat.moveTo(chat.txt_chat.getLength());
						chat.txt_chat.requestFollowCaret();
						if (!userName.equals(chat.user.getUsername())) {
							if (!chat.muted) {
								playSound("intuition.mp3");
							}
						}
					}

				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}

	}

	private void handleLogin(Packet00Login packet, InetAddress address, int port) {
		@SuppressWarnings("unused")
		UserMP user = new UserMP(packet.getUserName(), address, port);

	}

	public void sendData(byte[] data) {
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, 1337);
		try {
			socket.send(packet);
		} catch (IOException e) {
			System.err.println("sendData error");
		} catch (NullPointerException e) {
			System.err.println("null hiba");
		}
	}

	private void playSound(String file) {

		URL fileUrl = ChatClient.class.getResource(file);
		Media hit = new Media(fileUrl.toString());
		MediaPlayer mediaPlayer = new MediaPlayer(hit);
		mediaPlayer.play();

	}

	public void closeClient() {
		this.socket.disconnect();
		this.socket.close();
	}

}