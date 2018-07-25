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

    Button getRemoveButton(){ return removeButton; }
    Button getSelectButton(){ return selectButton; }
    TextField getField(){ return field; }

    Selector(final Stage stage, GridPane innerGrid){
        field.setPrefWidth(800);
        field.setText(Value.NoDirectoryLabel);

        selectButton.setPrefWidth(100);
        selectButton.setText(Value.SelectBtn);
        selectButton.setOnAction(event -> directoryChooser(stage));

        removeButton.setText(Value.RemoveBtn);
        removeButton.setOnAction(event -> removeSelector(innerGrid));
    }

    /**
     * Starts the directory chooser to select folder
     * @param stage Stage required for directory chooser
     */
    private void directoryChooser(final Stage stage){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        String text = selectedDirectory == null ? Value.NoDirectoryLabel : selectedDirectory.getAbsolutePath();
        field.setText(text);
    }

    /**
     * Starts removing of this at calculated row index
     * @param innerGrid grid to remove from
     */
    private void removeSelector(GridPane innerGrid){
        int rowIndex = GridPane.getRowIndex(field);
        deleteRow(innerGrid, rowIndex);
    }

    /**
     * Method which actually removes elements
     * @param grid grid to remove from
     * @param row row index
     */
    private void deleteRow(GridPane grid, final int row) {
        Set<Node> deleteNodes = new HashSet<>();
        for (Node child : grid.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(child);

            int r = (rowIndex == null) ? 0 : rowIndex;

            if (r > row) GridPane.setRowIndex(child, r-1);
            else if (r == row) deleteNodes.add(child);
        }
        grid.getChildren().removeAll(deleteNodes);
        SelectorUI.CheckAddRemove(grid);
    }

}