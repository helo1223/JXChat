package com.Helo.chat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

//JavaFX

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
//import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Chat extends Application {
	private double xOffset = 0;
	private double yOffset = 0;
	public Stage Stage;
    public String theme1Url = getClass().getResource("application.css").toExternalForm();
    public String theme2Url = getClass().getResource("application2.css").toExternalForm();
    public String theme = "";
	
	@Override
	public void start(Stage stage) throws Exception {
		// JavaFX ablak felépítése
		Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
		stage.initStyle(StageStyle.UNDECORATED);
		// Egérgombot lenyomva mozgatható legyen az ablak

		root.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				xOffset = event.getSceneX();
				yOffset = event.getSceneY();
			}
		});

		root.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				stage.setX(event.getScreenX() - xOffset);
				stage.setY(event.getScreenY() - yOffset);
			}
		});

		//JavaFX esetében nem Window és Frame van, hanem Stage és Scene
		Scene scene = new Scene(root);
		// Stílusfájl betöltése
		readConfig();
        scene.getStylesheets().add(theme);
        
		stage.setScene(scene);
		stage.getIcons().add(new Image(Chat.class.getResourceAsStream("icon.png")));
		stage.show();
		Stage = stage;
		

	}

	public static void main(String[] args) {
		launch(args);


	}
	

	
	public void readConfig() {
		try {
		String path = "./config.ini";
		FileInputStream  file = new FileInputStream(path);
		BufferedReader br = new BufferedReader(new InputStreamReader(file));
		String conf = br.readLine();
		if (conf.equals("0")) {
			theme=theme1Url;
		}else if(conf.equals("1")) {
			theme=theme2Url;
		}
		}catch(Exception e) {
			theme = theme1Url;
		}
	}

	
}
