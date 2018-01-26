package com.youos.hoconeditor.selector;

import com.youos.hoconeditor.ConfigManager;
import com.youos.hoconeditor.editor.EditorUI;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class SelectorUI extends Application {

    private GridPane outerGrid = new GridPane(),
                     innerGrid = new GridPane();

    public static void main(String[] args){launch();}

    /**
     * Builds frontend of first window, the selectorStage to select directories
     * @param primaryStage Stage to build on
     */
    @Override
    public void start(final Stage primaryStage) {
        primaryStage.setTitle("HOCON Viewer");

        outerGrid.setAlignment(Pos.CENTER);
        outerGrid.setHgap(10);
        outerGrid.setVgap(5);
        outerGrid.setPadding(new Insets(25, 25, 25, 25));

        innerGrid.setAlignment(Pos.CENTER);
        innerGrid.setHgap(10);
        innerGrid.setVgap(5);

        GridPane start_addGrid = new GridPane();
        start_addGrid.setAlignment(Pos.CENTER);
        start_addGrid.setHgap(5);

        Text sceneTitle = new Text("Search folder:");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));

        Button startBtn = new Button();
        startBtn.setText("OK");
        startBtn.setPrefWidth(100);

        Button addBtn = new Button();
        addBtn.setText("+");

        Button[] start_addBtns = prepareActionListener(startBtn, addBtn, primaryStage);

        start_addGrid.add(start_addBtns[0], 0, 0);
        start_addGrid.add(start_addBtns[1], 1, 0);
        outerGrid.add(sceneTitle, 0, 0);
        outerGrid.add(start_addGrid, 0, 2);
        addSelector(primaryStage);

        Scene scene = new Scene(outerGrid, 900, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Adds Selector pair of textField and button for the user
     * @param stage Stage to help building the selector
     */
    private void addSelector(Stage stage){
        Selector selector = new Selector(stage);

        int rowCount = getRowCount(innerGrid);

        innerGrid.add(selector.getField(), 0, rowCount);
        innerGrid.add(selector.getButton(), 1, rowCount);

        outerGrid.getChildren().remove(innerGrid);
        outerGrid.add(innerGrid, 0, 1, 2, 1);
    }

    /**
     * @param grid GridPane to be analyzed
     * @return row count of grid
     */
    private int getRowCount(GridPane grid) {
        int numRows = grid.getRowConstraints().size();
        for (int i = 0; i < grid.getChildren().size(); i++) {
            Node child = grid.getChildren().get(i);
            if (child.isManaged()) {
                Integer rowIndex = GridPane.getRowIndex(child);
                if(rowIndex != null){
                    numRows = Math.max(numRows,rowIndex + 1);
                }
            }
        }
        return numRows;
    }

    /**
     * @param grid Gridpane containing textFields
     * @return Array with textFields found in grid
     */
    private TextField[] getAllTextFields(GridPane grid) {
        TextField[] fields = new TextField[getRowCount(grid)];
        ObservableList<Node> children = grid.getChildren();
        for (Node node : children) {
            if(GridPane.getColumnIndex(node) == 0) {
                fields[GridPane.getRowIndex(node)] = (TextField) node;
            }
        }
        return fields;
    }

    /**
     * Puts Action Listener on start and add button
     *
     * @param startBtn button to start manager
     * @param addBtn button to add directions
     * @param primaryStage Stage of this window
     * @return Array with buttons that have action listeners
     */
    private Button[] prepareActionListener(Button startBtn, Button addBtn, final Stage primaryStage){
        addBtn.setOnAction(event -> addSelector(primaryStage));

        startBtn.setOnAction(event -> {

            //Check if every textField holds a valid existing path
            ArrayList<Path> finalDirections = new ArrayList<>();
            for (TextField field : getAllTextFields(innerGrid)) finalDirections.add(Paths.get(field.getText()));
            for (Path path : finalDirections){
                if (Files.notExists(path)){
                    EditorUI.showAlert("Error", null, "The directory \"" + path.toString() + "\" does not exist!", Alert.AlertType.ERROR);
                    return;
                }
            }

            //Start ConfigManager and continue if he has finished reading the files
            ConfigManager manager = new ConfigManager(finalDirections);
            if (manager.isReady()){

                //Hide selector window
                primaryStage.hide();

                //Open editor window
                new EditorUI(manager, primaryStage);
            }
        });

        return new Button[]{startBtn, addBtn};
    }
}
