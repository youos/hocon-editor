package com.youos.hoconeditor.editor;

import com.typesafe.config.ConfigObject;
import com.youos.hoconeditor.ConfigManager;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.ArrayList;


/*

0. Struktur
1. Fehlermeldung bei mehreren application.confs
2. Fehlermeldungen generell
3. Eintrag löschen Backend

 */


public class EditorUI {

    private Stage selectorStage;
    private Stage mainStage;

    private TextField valueField = new TextField();
    private Text pathField = new Text();
    private Text typeField = new Text();
    private TextArea fileField = new TextArea();
    private TextArea commentField = new TextArea();

    private Button editBtn = new Button("Edit");
    private Button openBtn = new Button("Open New Directory");
    private Button saveBtn = new Button("Apply Changes");
    private Button deleteBtn = new Button("Delete Entry");

    private TreeView<String> tree;

    private ConfigManager configManager;
    private Editor editor;

    private TreeView<String> buildTree(){
        return new Tree(this, configManager).getTreeView();
    }

    public EditorUI(ArrayList<Path> dir, Stage selectorStage){
        this.selectorStage = selectorStage;
        this.configManager = new ConfigManager(dir);
        this.editor = new Editor();

        pathField.setFill(Color.RED);
        valueField.setDisable(true);
        valueField.setMaxWidth(300);
        typeField.setFill(Color.GREEN);
        fileField.setMaxSize(300, 40);
        fileField.setEditable(false);
        commentField.setMaxSize(300, 40);
        commentField.setEditable(false);
        editBtn.setDisable(true);
        editBtn.setPrefWidth(70);

        Label pathLabel = new Label("Path : ");
        Label valueLabel = new Label("Value : ");
        Label typeLabel = new Label("Type : ");
        Label fileLabel = new Label("File : ");
        Label commentLabel = new Label("Comment : ");

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

        tree = buildTree();
        prepareEvents();

        ToolBar toolBar = new ToolBar(openBtn, saveBtn, deleteBtn);
        toolBar.setPrefWidth(900);
        GridPane toolGrid = new GridPane();
        toolGrid.setMaxHeight(toolBar.getHeight());
        toolGrid.add(toolBar, 0, 0);
        SplitPane hSplitter = new SplitPane();
        hSplitter.getItems().addAll(tree, editPane);
        SplitPane vSplitter = new SplitPane();
        SplitPane.setResizableWithParent(vSplitter, false);
        vSplitter.setOrientation(Orientation.VERTICAL);
        vSplitter.getItems().addAll(toolGrid, hSplitter);
        mainStage = new Stage();
        mainStage.setTitle(selectorStage.getTitle());
        mainStage.setScene(new Scene(vSplitter, 900, 800));
        mainStage.show();
    }

    public void changeEditingEntry(TreeItem<String> item, ConfigObject config){

        editor.setup(item, config);

        String file = editor.getFile();
        String path = editor.getPath();
        String comment = editor.getComment();
        String type = editor.getType();
        String value = editor.getValue();
        Boolean btnDisabled = editor.getBtnDisabled();

        editBtn.setText("EDIT");
        editBtn.setDisable(btnDisabled);

        fileField.setText(file);
        pathField.setText(path);
        commentField.setText(comment);
        typeField.setText(type);
        valueField.setText(value);
    }

    private void deleteSelectedEntry(){
        //TODO Delete selected Entry from Tree
    }

    private void selectNewFolders(){
        mainStage.hide();
        selectorStage.show();
    }

    private void prepareEvents(){
        editBtn.setOnAction((ActionEvent event) -> {
            if (valueField.isDisabled()){
                valueField.setDisable(false);
                editBtn.setText("OK");
            } else {

                //Edit Entry

                valueField.setDisable(true);

                //Rebuild Backend
                editor.editEntryInConfig(valueField.getText(), configManager);

                //Rebuild Frontend
                changeEditingEntry(editor.getItem(), configManager.getFullConfig());
                tree = buildTree();
            }
        });
        openBtn.setOnAction(event -> selectNewFolders());
        saveBtn.setOnAction(event -> configManager.saveDataToFile());
        deleteBtn.setOnAction(event -> deleteSelectedEntry());
    }

}
