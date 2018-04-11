package org.alixia.games.diplomacy;

import javafx.application.Application;
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
		primaryStage.show();
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

}
