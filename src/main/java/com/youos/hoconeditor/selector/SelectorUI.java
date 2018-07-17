package com.youos.hoconeditor.selector;

import com.youos.hoconeditor.ConfigManager;
import com.youos.hoconeditor.Value;
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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class SelectorUI extends Application {

    public static void main(String[] args){launch();}

    /**
     * Builds frontend of first window, the selectorStage to select directories
     * @param primaryStage Stage to build on
     */
    @Override
    public void start(final Stage primaryStage) {
        primaryStage.setTitle(Value.WindowTitle);

        GridPane mainGrid = initializeGrid(primaryStage);

        Scene scene = new Scene(mainGrid, 900, 400);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Adds Selector pair of textField and button for the user
     * @param stage Stage to help building the selector
     */
    private void addSelector(Stage stage, GridPane innerGrid, GridPane outerGrid){
        Selector selector = new Selector(stage, innerGrid);

        int rowCount = GetRowCount(innerGrid);

        innerGrid.add(selector.getField(), 0, rowCount);
        innerGrid.add(selector.getSelectButton(), 1, rowCount);
        innerGrid.add(selector.getRemoveButton(), 2, rowCount);

        outerGrid.getChildren().remove(innerGrid);
        outerGrid.add(innerGrid, 0, 1, 2, 1);

        CheckAddRemove(innerGrid);
    }

    static void CheckAddRemove(GridPane grid){
        if (SelectorUI.GetRowCount(grid) == 1){
            grid.getChildren().get(2).setDisable(true);
        } else {
            for (Node child : grid.getChildren()) {
                if (GridPane.getColumnIndex(child) == 2) child.setDisable(false);
            }
        }
    }

    /**
     * @param grid GridPane to be analyzed
     * @return row count of grid
     */
    private static int GetRowCount(GridPane grid) {
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
     * @param grid GridPane containing textFields
     * @return Array with textFields found in grid
     */
    private static TextField[] getAllTextFields(GridPane grid) {
        TextField[] fields = new TextField[GetRowCount(grid)];
        ObservableList<Node> children = grid.getChildren();
        for (Node node : children) {
            if(GridPane.getColumnIndex(node) == 0) {
                fields[GridPane.getRowIndex(node)] = (TextField) node;
            }
        }
        return fields;
    }

    private void requestStartEditing(final Stage primaryStage, GridPane innerGrid){

        ArrayList<Path> paths = new ArrayList<>();
        for (TextField field : getAllTextFields(innerGrid)) paths.add(Paths.get(field.getText()));
        removeDuplicates(paths);
        if (areValid(paths)){
            new ConfigManager(paths, primaryStage);
        }
    }

    private boolean areValid(ArrayList<Path> paths){
        for (Path path : paths){
            boolean noDir = path.toString().equals(Value.NoDirectoryLabel);
            if (noDir) continue;
            if (Files.notExists(path)){
                String text = Value.InvalidDirectory(path.toString());
                EditorUI.showAlert("Error", null, text, Alert.AlertType.ERROR);
                return false;
            }
        }
        paths.remove(Paths.get(Value.NoDirectoryLabel));
        if (paths.size() == 0){
            EditorUI.showAlert("Error", null, Value.NoDirectoryError, Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void removeDuplicates(ArrayList<Path> list){
        LinkedHashSet<Path> setItems = new LinkedHashSet<>(list);
        list.clear();
        list.addAll(setItems);
    }

    private GridPane initializeGrid(final Stage primaryStage){

        GridPane outerGrid = new GridPane(),
                innerGrid = new GridPane(),
                start_addGrid = new GridPane();

        Text sceneTitle = new Text();

        Button startBtn = new Button(),
                addBtn = new Button();

        styleSetup(innerGrid, outerGrid, start_addGrid, sceneTitle, startBtn, addBtn);

        startBtn.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                requestStartEditing(primaryStage, innerGrid);
        });
        startBtn.setOnAction(event -> requestStartEditing(primaryStage, innerGrid));
        addBtn.setOnAction(event -> addSelector(primaryStage, innerGrid, outerGrid));

        start_addGrid.add(startBtn, 0, 0);
        start_addGrid.add(addBtn, 1, 0);
        outerGrid.add(sceneTitle, 0, 0);
        outerGrid.add(start_addGrid, 0, 2);
        addSelector(primaryStage, innerGrid, outerGrid);

        return outerGrid;
    }

    private void styleSetup(GridPane innerGrid, GridPane outerGrid, GridPane start_addGrid, Text sceneTitle, Button startBtn, Button addBtn) {
        outerGrid.setAlignment(Pos.CENTER);
        outerGrid.setHgap(10);
        outerGrid.setVgap(5);
        outerGrid.setPadding(new Insets(25, 25, 25, 25));

        innerGrid.setAlignment(Pos.CENTER);
        innerGrid.setHgap(10);
        innerGrid.setVgap(5);

        start_addGrid.setAlignment(Pos.CENTER);
        start_addGrid.setHgap(5);

        sceneTitle.setText(Value.SceneTitle);
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));

        startBtn.setText(Value.StartBtn);
        startBtn.setPrefWidth(100);

        addBtn.setText(Value.AddBtn);
    }
}