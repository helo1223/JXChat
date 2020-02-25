package com.Helo.chat;

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

		Scene scene = new Scene(root);
		// Stílusfájl betöltése
		scene.getStylesheets().addAll(Chat.class.getResource("application.css").toExternalForm());
		stage.setScene(scene);
		stage.getIcons().add(new Image(Chat.class.getResourceAsStream("icon.png")));
		stage.show();
		Stage = stage;

	}

	public static void main(String[] args) {
		launch(args);

	}

}
