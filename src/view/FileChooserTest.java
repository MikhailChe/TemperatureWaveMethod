package view;

import java.io.File;

import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class FileChooserTest extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage mainStage) throws Exception {
		mainStage.setOnCloseRequest(e -> mainStage.close());

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open resource File");
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("Text Files", "*.txt"),
				new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
				new ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
				new ExtensionFilter("All Files", "*.*"));
		File selectedFile = fileChooser.showOpenDialog(mainStage);
		System.out.println(selectedFile);
	}

}
