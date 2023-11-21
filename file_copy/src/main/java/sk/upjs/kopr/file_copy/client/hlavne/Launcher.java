package sk.upjs.kopr.file_copy.client.hlavne;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application{
	
	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/hlavneokno.fxml"));
		HlavneOknoController hlavneOknoController = new HlavneOknoController();
		fxmlLoader.setController(hlavneOknoController);
		
		Parent parent = fxmlLoader.load();
		Scene scene = new Scene(parent);
		
		//https://stackoverflow.com/questions/44439408/javafx-controller-detect-when-stage-is-closing
		stage.setOnCloseRequest(evt -> hlavneOknoController.close());
		
		stage.setScene(scene);
		stage.setTitle("KOPR filecopy client");
		stage.show();
		

	}

	public static void main(String[] args) {
		launch(args);
	}

}
