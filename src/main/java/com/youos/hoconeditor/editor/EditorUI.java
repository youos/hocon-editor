package com.youos.hoconeditor.editor;

import com.typesafe.config.Config;
import com.youos.hoconeditor.ConfigManager;
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

    private TextArea fileField = new TextArea();
    private Text pathField = new Text();
    private TextArea commentField = new TextArea();
    private Text typeField = new Text();
    private TextArea valueField = new TextArea();

    private Button editBtn = new Button("Edit");
    private Button openBtn = new Button("Open New Directory");
    private Button saveBtn = new Button("Apply Changes");
    private Button deleteBtn = new Button("Delete Entry");

    private ConfigManager configManager;
    private Editor editor;
    private Tree tree;



    public EditorUI(ConfigManager manager, Stage selectorStage){
        this.selectorStage = selectorStage;
        this.configManager = manager;
        this.editor = new Editor();
        this.tree = new Tree(this, configManager);

        //Setting properties for Frontend elements of the editor
        fileField.setMaxSize(300, 40);
        fileField.setEditable(false);
        pathField.setFill(Color.RED);
        commentField.setMaxSize(300, 40);
        typeField.setFill(Color.GREEN);
        valueField.setMaxSize(300, 40);
        editBtn.setDisable(true);
        editBtn.setPrefSize(70, 40);
        deleteBtn.setDisable(true);

        Label pathLabel = new Label("Path : ");
        Label valueLabel = new Label("Value : ");
        Label typeLabel = new Label("Type : ");
        Label fileLabel = new Label("File : ");
        Label commentLabel = new Label("Comment : ");

        //Add elements to a grid for ordered view
        GridPane editPane = new GridPane();
        editPane.setVgap(10);
        editPane.setHgap(10);
        editPane.setAlignment(Pos.TOP_CENTER);
        editPane.setPadding(new Insets(25, 25, 25, 25));
        editPane.add(fileLabel, 0, 0);
        editPane.add(fileField, 1, 0);
        editPane.add(pathLabel, 0, 1);
        editPane.add(pathField, 1, 1);
        editPane.add(commentLabel, 0, 2);
        editPane.add(commentField, 1, 2);
        editPane.add(typeLabel, 0, 3);
        editPane.add(typeField, 1, 3);
        editPane.add(valueLabel, 0, 4);
        editPane.add(valueField, 1, 4);
        editPane.add(editBtn, 2, 4);

        //Action listener
        prepareEvents();

        /*Basic view:
         * Content A: Toolbar
         * Content B: Tree -- Editor
         * Split Contents vertically
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
        Boolean btnDisabled = editor.getBtnDisabled();

        //Set them into view
        fileField.setText(file);
        pathField.setText(path);
        commentField.setText(comment);
        typeField.setText(type);
        valueField.setText(value);

        editBtn.setDisable(btnDisabled);
        deleteBtn.setDisable(false);
    }

    private void selectNewFolders(){

        //Close window and open selectorStage to select new directories
        mainStage.hide();
        selectorStage.show();

    }

    private void prepareEvents(){

        //Action events for toolbar and edit buttons
        editBtn.setOnAction(event -> editEntry());
        openBtn.setOnAction(event -> selectNewFolders());
        saveBtn.setOnAction(event -> configManager.saveDataToFile());
        deleteBtn.setOnAction(event -> {

            //Confirm before continue removing the element
            if (showAlert("Confirm", "Are you sure you want to remove this entry?",
                    "Key to be removed: \n" + editor.getPath(), Alert.AlertType.CONFIRMATION)) {

                //Remove item in Backend (both config variables)
                editor.deleteSelectedEntry(configManager);

                //Remove item in Frontend (visual TreeView)
                tree.remove(editor.getItem());
            }
        });
    }

    private void editEntry(){

        //Rebuild Backend
        editor.editEntryInConfig(valueField.getText(), commentField.getText(), configManager);

        //Rebuild Frontend
        changeEditingEntry(editor.getItem(), configManager.getFullConfig());
    }



    public static boolean showAlert(String title, String header, String content, Alert.AlertType type){

        //Show alert for warnings, problems, errors (specified by type)
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == ButtonType.OK;
    }

}
