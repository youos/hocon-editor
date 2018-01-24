package com.youos.hoconeditor.selector;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class Selector {

    private Button button = new Button();

    private TextField field = new TextField("C:\\Users\\Growthteam\\Desktop\\test-data\\resources");

    Selector(final Stage stage){
        field.setPrefWidth(800);
        //field.setText("No directory selected");
        button.setPrefWidth(100);
        button.setText("Select");
        button.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File selectedDirectory = directoryChooser.showDialog(stage);
                if(selectedDirectory == null){
                    field.setText("No directory selected");
                }else{
                    field.setText(selectedDirectory.getAbsolutePath());
                }
            }
        });
    }

    public Button getButton(){
        return button;
    }

    public TextField getField(){
        return field;
    }

}
