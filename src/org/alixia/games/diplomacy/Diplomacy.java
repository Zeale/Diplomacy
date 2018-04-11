package org.alixia.games.diplomacy;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Diplomacy extends Application {

	public Diplomacy() {
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Two possible window size options exist, the first being a 1028x1028 size
		// window, and the second being a 514x514 size window, for now.
		primaryStage.setWidth(1028);
		primaryStage.setHeight(1028);
		Board board = new Board();
		board.setPrefWidth(1028);
		board.setPrefHeight(1028);
		primaryStage.setScene(new Scene(board));
		primaryStage.show();
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

}
