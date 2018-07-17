package com.youos.hoconeditor.selector;

import com.youos.hoconeditor.Value;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Class Selector:
 *
 * Class to create an Object representing the elements
 * in one row of the selection grid
 */

class Selector {

    private Button selectButton = new Button();
    private Button removeButton = new Button();
    private TextField field = new TextField();

    Selector(final Stage stage, GridPane innerGrid){
        field.setPrefWidth(800);
        field.setText(Value.NoDirectoryLabel);
        field.setText("C:\\Users\\Growthteam\\Documents\\IdeaProjects\\hocontool\\src\\main\\resources");

        selectButton.setPrefWidth(100);
        selectButton.setText(Value.SelectBtn);
        selectButton.setOnAction(event -> directoryChooser(stage));

        removeButton.setText(Value.RemoveBtn);
        removeButton.setOnAction(event -> removeSelector(innerGrid));
    }

    private void directoryChooser(final Stage stage){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        String text = selectedDirectory == null ? Value.NoDirectoryLabel : selectedDirectory.getAbsolutePath();
        field.setText(text);
    }

    private void removeSelector(GridPane innerGrid){
        int rowIndex = GridPane.getRowIndex(field);
        deleteRow(innerGrid, rowIndex);
    }

    private void deleteRow(GridPane grid, final int row) {
        Set<Node> deleteNodes = new HashSet<>();
        for (Node child : grid.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(child);

            int r = rowIndex == null ? 0 : rowIndex;

            if (r > row) GridPane.setRowIndex(child, r-1);
            else if (r == row) deleteNodes.add(child);
        }
        grid.getChildren().removeAll(deleteNodes);
        SelectorUI.CheckAddRemove(grid);
    }

    Button getRemoveButton(){ return removeButton; }
    Button getSelectButton(){ return selectButton; }
    TextField getField(){ return field; }

}