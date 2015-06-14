package gui;


import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class PackerGUI extends Application implements MessageListener {

	private ObservableList<String> listViewData;
	private TextField browseTextField;
	
	private final Settings settings;
	
	public PackerGUI() throws IOException {
		this.settings = new Settings();
	}
	
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("SMW Packer/Unpacker");

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		browseTextField = new TextField();
		browseTextField.setPrefWidth(300);
		grid.add(browseTextField, 0, 0);

		final FileChooser fileChooser = new FileChooser();
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		
		final  Button browseDirectoryButton = new Button("Browse Directory...");
		final  Button browseFileButton = new Button("Browse File...");
		
		browseDirectoryButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				directoryChooser.setInitialDirectory(new File(settings.getProperty(Settings.CHOOSER_DIR)));
				
				final File file = directoryChooser.showDialog(primaryStage);	
				if(file != null) {
					browseTextField.setText(file.getAbsolutePath());
					settings.setProperty(Settings.CHOOSER_DIR, file.getParent());
				}
			}
		});
	
		browseFileButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				
				fileChooser.setInitialDirectory(new File(settings.getProperty(Settings.CHOOSER_DIR)));
				
				final File file = fileChooser.showOpenDialog(primaryStage);	
				if(file != null) {
					browseTextField.setText(file.getAbsolutePath());
					settings.setProperty(Settings.CHOOSER_DIR, file.getParent());
				}
			
			}
			
		});
		
		grid.add(browseDirectoryButton, 1, 0);
		grid.add(browseFileButton, 2, 0);


		listViewData = FXCollections.observableArrayList();
		final ListView<String> listView = new ListView<String>(listViewData);
		listView.setPrefSize(200, 250);
		listView.setEditable(true);
		listView.setItems(listViewData);       
		grid.add(listView, 0, 1,1,2);

		final  Button packButton = new Button("Pack");
		packButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				sendMessage("eric");
			}
		});

		final  Button unpackButton = new Button("Unpack");

		final VBox vbox = new VBox(10);

		vbox.setAlignment(Pos.BOTTOM_LEFT);
		vbox.getChildren().add(packButton);
		vbox.getChildren().add(unpackButton);

		grid.add(vbox, 1, 1);

		final Scene scene = new Scene(grid, 800, 275);
		primaryStage.setScene(scene);

		primaryStage.show();
	}

	
	@Override
	public void stop(	) throws Exception {
		super.stop();
		
		settings.save();
	}
	
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void sendMessage(String message) {
		listViewData.add(message);
		
	}
}