//F� �llom�ny, ez kezeli az �sszes gombot, valamint esem�nyt

package com.Helo.chat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.controlsfx.control.ToggleSwitch;
import org.fxmisc.richtext.StyleClassedTextArea;

import com.Helo.chat.net.ChatClient;
import com.Helo.chat.net.ChatServer;
import com.Helo.chat.net.packets.Packet00Login;
import com.Helo.chat.net.packets.Packet01Disconnect;
import com.Helo.chat.net.packets.Packet02Message;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class Controller implements Initializable {

	// A deklar�l�s el�tti @FXML az�rt sz�ks�ges, mert a Scene Builder csak �gy
	// l�tja a programon bel�l az elemeket

	@FXML
	private ImageView btn_ujszerver, btn_csatlakozas, btn_lecsatlakozas, btn_kilepes, btn_beallit, btn_mute, btn_unmute,
			btn_mute1, btn_unmute1;
	@FXML
	public Pane pn_menu, pn_chat, pn_home;
	@FXML
	public Pane pn_beallit;
	@FXML
	public Pane pn_custom;
	@FXML
	private TextField txt_message;
	@FXML
	private TextField txt_ip;
	@FXML
	private TextField txt_username;
	@FXML
	public StyleClassedTextArea txt_chat;
	@SuppressWarnings("rawtypes")
	@FXML
	private ListView list_users;
	@FXML
	private Button btn_beallit2;
	@FXML
	private Text txt_menu_title;
	@FXML
	private Text txt_submenu_title;
	@FXML
	private Text txt_tip;
	@FXML
	private ColorPicker pick_menu;
	@FXML
	private ColorPicker pick_pane;
	@FXML
	private ColorPicker pick_chat;
	@FXML
	private ColorPicker pick_message;
	@FXML
	private ColorPicker pick_user1;
	@FXML
	private ColorPicker pick_user2;
	@FXML
	private ToggleSwitch toggleswitch;

	String option = "";

	public Chat chat;
	public ChatClient socketClient;
	public ChatServer socketServer;
	private String userName;
	public User user;
	private static String ip;
	public boolean csatlakozva = false;
	public Text t;
	private long connTime;

	public boolean muted = false;
	public List<String> userslist = new ArrayList<>();
	public ListProperty<String> listProperty = new SimpleListProperty<>();

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		list_users.itemsProperty().bind(listProperty);
		listProperty.set(FXCollections.observableArrayList(userslist));

		Tooltip.install(btn_ujszerver, new Tooltip("�j szerver"));
		Tooltip.install(btn_csatlakozas, new Tooltip("Csatlakoz�s"));
		Tooltip.install(btn_beallit, new Tooltip("Be�ll�t�sok"));
		Tooltip.install(btn_lecsatlakozas, new Tooltip("Lecsatlakoz�s"));
		Tooltip.install(btn_kilepes, new Tooltip("Kil�p�s"));

		txt_chat.setEditable(false);
		txt_chat.setId("txt_chat");
		txt_message.setDisable(true);
		readConfig();
	}

	// Az esem�nyek deklar�l�sa el�tt is sz�ks�ges a @FXML, hogy hozz�rendelhess�k
	// gombokhoz �ket

	@FXML
	public void sendMessage(KeyEvent ke) {
		// Hiba: A felhaszn�l� megpr�b�lhat �zenetet k�ldeni akkor is, ha nincs
		// csatlakozva egy szob�hoz
		// Fix: Ellen�rizz�k, hogy csatlakozva van-e miel�tt elk�ldj�k az �zenetet
		// Hiba: �res �zeneteket is enged�lyez a program
		// Fix: Ellen�rizz�k, hogy az �zenet �res-e, �s csak akkor k�ldj�k ki, ha nem
		if (ke.getCode().equals(KeyCode.ENTER) && csatlakozva && !txt_message.getText().equals("")) {
			Packet02Message packet = new Packet02Message(user.getUsername(), txt_message.getText());
			packet.writeData(socketClient);
			txt_message.setText("");
		}
	}

	@FXML
	public void kilepes() {
		if (csatlakozva) {
			Packet01Disconnect packet = new Packet01Disconnect(user.getUsername());
			packet.writeData(socketClient);
		}
		System.exit(0);
	}

	@FXML
	public void beallit2() {
		// Kit�lt�tt sz�vegmez�b�l �tvessz�k az adatokat
		ip = txt_ip.getText();
		userName = txt_username.getText();
		txt_menu_title.setVisible(false);
		txt_submenu_title.setVisible(false);
		txt_tip.setVisible(false);
		// Sz�vegek be�ll�t�sa att�l f�gg�en, hogy a felhaszn�l� melyik gombra nyomott
		// (Csatlakoz�s vagy �j szerver)
		switch (option) {
		case "ujSzerver":
			ujSzerver();

			break;
		case "csatlakozas":

			csatlakozas();
			break;
		default:
			System.out.println("valami nem j�");
			break;
		}

	}

	@FXML
	public void ujSzerverpre() {
		pn_beallit.toFront();
		pn_beallit.setVisible(true);
		txt_menu_title.setText("�j szerver");
		txt_submenu_title.setText("Hozz l�tre �j cseveg�szob�t");
		txt_tip.setText("Gy�z�dj meg r�la, hogy az 1337-es port nyitva van!");
		txt_menu_title.setVisible(true);
		txt_submenu_title.setVisible(true);
		txt_tip.setVisible(true);
		option = "ujSzerver";
	}

	public void ujSzerver() {
		pn_home.toBack();
		pn_home.setVisible(false);
		txt_message.setDisable(false);

		try {
			socketServer = new ChatServer(this);
			socketServer.start();
			socketClient = new ChatClient(this, ip);
			socketClient.start();
			//Szerver megnyit�sakor l�trehozunk egy felhaszn�l�t �s ell�tjuk a "host" jelz�vel
			user = new UserMP(userName.concat("#host"), null, 0);
			Packet00Login loginPacket = new Packet00Login(user.getUsername());
			if (socketServer != null) {
				socketServer.addConnection((UserMP) user, loginPacket);
			}
			loginPacket.writeData(socketClient);

			csatlakozva = true;
			pn_chat.toFront();
			pn_chat.setVisible(true);
		} catch (Exception e) {
			System.err.println();
		}

	}

	public void csatlakozaspre() {
		pn_beallit.toFront();
		pn_beallit.setVisible(true);
		txt_menu_title.setText("Csatlakoz�s");
		txt_submenu_title.setText("Csatlakozz egy megl�v� szob�hoz");
		txt_tip.setText(
				"Ha nem tudsz csatlakozni, ellen�rizd az IP c�met vagy k�rd a szerver�zemeltet�t, hogy ellen�rizze, nyitva van-e az 1337-es port.");
		txt_menu_title.setVisible(true);
		txt_submenu_title.setVisible(true);
		txt_tip.setVisible(true);
		option = "csatlakozas";

	}

	public void csatlakozas() {
		// A csatlakozott felhaszn�l� nev�hez hozz�f�z�nk egy 5 jegy� sz�mot, ami a
		// jelenlegi milliszekundumban m�rt id� utols� 5 sz�mjegye
		// Ez az�rt sz�ks�ges, mert ha a felhaszn�l� t�bbsz�ri le- majd
		// visszacsatlakoz�s ut�n megzavarta a chatsz�vegek st�lusbe�ll�t�s�t
		connTime = System.currentTimeMillis() % 100000;
		// Ha a felhaszn�l� m�r jelen van egy szob�ban, akkor el�sz�r kil�ptetj�k onnan
		if (csatlakozva) {
			Packet01Disconnect packet = new Packet01Disconnect(user.getUsername());
			packet.writeData(socketClient);
			socketClient.interrupt();
			csatlakozva = false;
			txt_chat.clear();
			userslist.clear();
			listProperty.clear();
			socketClient.closeClient();
		}
		txt_message.setDisable(false);
		socketClient = new ChatClient(this, ip);
		socketClient.start();
		user = new UserMP(userName.concat("#" + connTime), null, 0);
		Packet00Login loginPacket = new Packet00Login(user.getUsername());
		pn_home.toBack();
		pn_home.setVisible(false);
		pn_chat.toFront();
		pn_chat.setVisible(true);
		if (socketServer != null) {
			socketServer.addConnection((UserMP) user, loginPacket);
		}
		loginPacket.writeData(socketClient);
		csatlakozva = true;

	}

	public void lecsatlakozas() {
		if (csatlakozva) {
			try {
				Packet01Disconnect packet = new Packet01Disconnect(user.getUsername());
				packet.writeData(socketClient);
				csatlakozva = false;
				txt_chat.clear();
				userslist.clear();
				listProperty.clear();
				pn_home.toFront();
				pn_home.setVisible(true);
				txt_message.setDisable(true);
			} catch (Exception e) {

			} finally {
				socketServer.closeServer();
			}
		}
	}

	public void mute() {
		muted = true;
		btn_unmute.setVisible(false);
		btn_mute.setVisible(true);
		btn_unmute1.setVisible(false);
		btn_mute1.setVisible(true);
	}

	public void unmute() {
		muted = false;
		btn_unmute.setVisible(true);
		btn_mute.setVisible(false);
		btn_unmute1.setVisible(true);
		btn_mute1.setVisible(false);
	}
	boolean front = false;

	public void customize() {
		if(!front) {
		pn_custom.toFront();
		front = true;
		}else {
			pn_custom.toBack();
			front = false;
		}
	}
	
	public void readConfig() {
		try {
			String path = "./config.ini";
			FileInputStream  file = new FileInputStream(path);
			BufferedReader br = new BufferedReader(new InputStreamReader(file));
			String conf = br.readLine();
			
			if(conf.equals("0")) {
				toggleswitch.setSelected(false);

			}else if(conf.equals("1")) {
				toggleswitch.setSelected(true);

			}
			
		} catch(Exception e) {
			
		}
	}

	public void changeConfig() {
		boolean selected = toggleswitch.isSelected();
		try {

			FileWriter fw = new FileWriter("./config.ini");
			if(!selected) {
				fw.write("");
				fw.write("0");
				fw.close();
			}else if(selected) {
				fw.write("");
				fw.write("1");
				fw.close();
			}try {

			}catch(Exception e) {
				System.err.println(e);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}

	}
	
}
