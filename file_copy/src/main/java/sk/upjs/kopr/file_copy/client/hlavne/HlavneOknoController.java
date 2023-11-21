package sk.upjs.kopr.file_copy.client.hlavne;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
//import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import sk.upjs.kopr.file_copy.client.Client;
import javafx.beans.value.ChangeListener;


public class HlavneOknoController {

	/*
    @FXML
    private ProgressBar progressBar1;
    @FXML
    private ProgressBar progressBar2;
    */
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private TextField textfieldtcp;
    
    private Client klient;
    private int TCPConnections;
    private ExecutorService executor;
    
    BooleanProperty finished = new SimpleBooleanProperty();
    
    @FXML
	void initialize() {
    	stopButton.setDisable(true);
    	textfieldtcp.setText("1");
    	
    	finished.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					startButton.setDisable(false);
					stopButton.setDisable(true);
					
				} else {
					startButton.setDisable(true);
		    		stopButton.setDisable(false);
				}
			}
		});
    	finished.set(true);
    }
    
    @FXML
    void startCopyButtonAction(ActionEvent event) {
    	try {
    		TCPConnections = Integer.parseInt(textfieldtcp.getText());
    	}catch (Exception e) {
    		TCPConnections = 1;
		}

    	executor = Executors.newFixedThreadPool(TCPConnections);
    	klient = new Client(TCPConnections, executor);
    	System.out.println("Starting receiving with "+TCPConnections+" TCPs.");
    	finished.bind( klient.valueProperty());
    	klient.start();
    }
    
    
    
    @FXML
    void stopCopyButtonAction(ActionEvent event) {
    	klient.cancel();
    	executor.shutdownNow();
    	System.out.println("Stoping receiving.");
    }
    
    void close() {
    	if(klient != null)klient.cancel();
    	if(executor != null)executor.shutdownNow();
    	System.out.println("Closing App.");
    }
    
}
