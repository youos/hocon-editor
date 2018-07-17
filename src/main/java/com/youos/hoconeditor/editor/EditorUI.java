package com.youos.hoconeditor.editor;

import com.typesafe.config.Config;
import com.youos.hoconeditor.ConfigManager;
import com.youos.hoconeditor.Value;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Class EditorUI:
 *
 * This class manages the whole user interface containing -->
 *      TreeView to the left
 *      Editor grid to the right
 *      Toolbar on the top
 *
 * and the setup method for alerts
 *
 * Events triggered in this interface lead to Editor.java in most cases
 *
 */


public class EditorUI {

    private Stage selectorStage;
    private Stage mainStage;

    private Text fileField = new Text();
    private Text pathField = new Text();
    private Text typeField = new Text();
    private TextArea commentField = new TextArea();
    private TextField valueField = new TextField();
    private TextField environmentField = new TextField();

    private Button editBtn = new Button(Value.EditBtn);
    private Button deleteBtn = new Button(Value.DeleteBtn);

    private ConfigManager configManager;
    private Editor editor;
    private Tree tree;



    public EditorUI(ConfigManager manager, Stage selectorStage){
        this.selectorStage = selectorStage;
        this.configManager = manager;
        this.editor = new Editor();
        this.tree = new Tree(this, configManager);

        //Setting properties for Frontend elements of the editor
        fileField.setWrappingWidth(300);
        fileField.setFill(Color.BLACK);
        pathField.setFill(Color.RED);
        typeField.setFill(Color.GREEN);
        commentField.setMaxSize(300, 40);
        valueField.setMaxSize(300, 40);
        environmentField.setMaxSize(300, 40);
        editBtn.setDisable(true);
        editBtn.setPrefSize(70, 40);
        deleteBtn.setDisable(true);

        Label fileLabel = new Label(Value.FileLabel);
        Label pathLabel = new Label(Value.PathLabel);
        Label typeLabel = new Label(Value.TypeLabel);
        Label commentLabel = new Label(Value.CommentLabel);
        Label valueLabel = new Label(Value.ValueLabel);
        Label environmentLabel = new Label(Value.EnvironmentLabel);

        //Add elements to a grid for ordered view
        GridPane editPane = new GridPane();
        GridPane.setHalignment(editBtn, HPos.RIGHT);
        editPane.setVgap(10);
        editPane.setHgap(10);
        editPane.setAlignment(Pos.TOP_CENTER);
        editPane.setPadding(new Insets(25, 25, 25, 25));
        editPane.add(fileLabel, 0, 0);
        editPane.add(fileField, 1, 0);
        editPane.add(pathLabel, 0, 1);
        editPane.add(pathField, 1, 1);
        editPane.add(typeLabel, 0, 2);
        editPane.add(typeField, 1, 2);
        editPane.add(commentLabel, 0, 3);
        editPane.add(commentField, 1, 3);
        editPane.add(valueLabel, 0, 4);
        editPane.add(valueField, 1, 4);
        editPane.add(environmentLabel, 0, 5);
        editPane.add(environmentField, 1, 5);
        editPane.add(editBtn, 1, 6);

        //Action events for toolbar and edit buttons
        editBtn.setOnAction(event -> editEntry());
        Button openBtn = new Button(Value.OpenBtn);
        openBtn.setOnAction(event -> selectNewFolders());
        Button saveBtn = new Button(Value.SaveBtn);
        saveBtn.setOnAction(event -> configManager.saveDataToFile());
        deleteBtn.setOnAction(event -> requestDelete());

        /*Basic view:
         * Content A: Toolbar
         * Content B: Tree -- Editor
         * Split Contents horizontally, ContentB vertically
         */
        ToolBar toolBar = new ToolBar(openBtn, saveBtn, deleteBtn);
        toolBar.setPrefWidth(900);
        GridPane toolGrid = new GridPane();
        toolGrid.setMaxHeight(toolBar.getHeight());
        toolGrid.add(toolBar, 0, 0);
        SplitPane hSplitter = new SplitPane();
        hSplitter.getItems().addAll(tree.getTreeView(), editPane);
        SplitPane vSplitter = new SplitPane();
        SplitPane.setResizableWithParent(vSplitter, false);
        vSplitter.setOrientation(Orientation.VERTICAL);
        vSplitter.getItems().addAll(toolGrid, hSplitter);

        //Setting up Stage and show it
        mainStage = new Stage();
        mainStage.setTitle(selectorStage.getTitle());
        mainStage.setScene(new Scene(vSplitter, 900, 800));
        mainStage.show();
    }

    void changeEditingEntry(TreeItem<String> item, Config config){

        //Editor setup to determine properties of TreeItem
        editor.setup(item, config);

        //Reading out properties
        String file = editor.getFile();
        String path = editor.getPath();
        String comment = editor.getComment();
        String type = editor.getType();
        String value = editor.getValue();
        String environment = editor.getEnvironment();
        Boolean btnDisabled = editor.getBtnDisabled();

        //Set them into view
        fileField.setText(file);
        pathField.setText(path);
        commentField.setText(comment);
        typeField.setText(type);
        valueField.setText(value);
        environmentField.setText(environment);

        editBtn.setDisable(btnDisabled);
        deleteBtn.setDisable(false);
    }

    private void selectNewFolders(){

        //Close window and open selectorStage to select new directories
        mainStage.hide();
        selectorStage.show();

    }

    private void editEntry(){

        //Rebuild Backend
        editor.editEntryInConfig(valueField.getText(), commentField.getText(), configManager);

        //Rebuild Frontend
        changeEditingEntry(editor.getItem(), configManager.getFullConfig());
    }

    private void requestDelete(){

        //Confirm before continue removing the element
        if (showAlert("Confirmation", Value.DeleteConfirmation,
                Value.DeleteConfirmation(editor.getPath()), Alert.AlertType.CONFIRMATION)) {

            //Remove item in Backend (both config variables)
            editor.deleteSelectedEntry(configManager);

            //Remove item in Frontend (visual TreeView)
            tree.remove(editor.getItem());
        }
    }



    public static boolean showAlert(String title, String header, String content, Alert.AlertType type){

        //Show alert for warnings, problems, errors (specified by type)
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return !result.isPresent() || result.get() == ButtonType.OK;
    }

}
