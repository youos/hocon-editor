package com.youos.hoconeditor.editor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigUtil;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import com.youos.hoconeditor.ConfigManager;
import com.youos.hoconeditor.Value;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * Class EditorUI:
 * <p>
 * This class manages the whole user interface containing -->
 * TreeView to the left
 * Editor grid to the right
 * Toolbar on the top
 * <p>
 * and the setup method for alerts
 * <p>
 * Events triggered in this interface lead to Editor.java in most cases
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
    private Button renameBtn = new Button(Value.RenameBtn);

    private ConfigManager configManager;
    private Editor editor;
    private Tree tree;


    public EditorUI(ConfigManager manager, Stage selectorStage) {
        this.selectorStage = selectorStage;
        this.configManager = manager;
        this.editor = new Editor();
        this.tree = new Tree(this, configManager);

        //Setting properties for Frontend elements of the editor
        fileField.setFill(Color.BLACK);
        pathField.setFill(Color.RED);
        typeField.setFill(Color.GREEN);
        commentField.setMaxSize(300, 40);
        valueField.setMaxSize(300, 40);
        environmentField.setMaxSize(300, 40);
        editBtn.setDisable(true);
        editBtn.setPrefSize(70, 40);
        deleteBtn.setDisable(true);
        renameBtn.setDisable(true);

        Label fileLabel = new Label(Value.FileLabel);
        Label pathLabel = new Label(Value.PathLabel);
        Label typeLabel = new Label(Value.TypeLabel);
        Label commentLabel = new Label(Value.CommentLabel);
        Label valueLabel = new Label(Value.ValueLabel);
        Label environmentLabel = new Label(Value.EnvironmentLabel);

        ScrollPane fileScrollPane = new ScrollPane();
        fileScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        fileScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        fileScrollPane.setMaxWidth(300);
        fileScrollPane.setPrefHeight(40);
        fileScrollPane.setContent(fileField);

        ScrollPane pathScrollPane = new ScrollPane();
        pathScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pathScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pathScrollPane.setMaxWidth(300);
        pathScrollPane.setPrefHeight(40);
        pathScrollPane.setBorder(Border.EMPTY);
        pathScrollPane.setStyle("-fx-border-width : 0");
        pathScrollPane.setContent(pathField);

        //Add elements to a grid for ordered view
        GridPane editPane = new GridPane();
        GridPane.setHalignment(editBtn, HPos.RIGHT);
        editPane.setVgap(10);
        editPane.setHgap(10);
        editPane.setAlignment(Pos.TOP_CENTER);
        editPane.setPadding(new Insets(25, 25, 25, 25));
        editPane.add(fileLabel, 0, 0);
        editPane.add(fileScrollPane, 1, 0);
        editPane.add(pathLabel, 0, 1);
        editPane.add(pathScrollPane, 1, 1);
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
        renameBtn.setOnAction(event -> requestRename());
        Button newKeyBtn = new Button(Value.NewKeyBtn);
        newKeyBtn.setOnAction(event -> requestNewKey());

        /*Basic view:
         * Content A: Toolbar
         * Content B: Tree -- Editor
         * Split Contents horizontally, ContentB vertically
         */
        ToolBar toolBar = new ToolBar(openBtn, saveBtn, deleteBtn, renameBtn, newKeyBtn);
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

    void changeEditingEntry(TreeItem<String> item, Config config) {

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
        renameBtn.setDisable(false);
    }

    private void selectNewFolders() {

        //Close window and open selectorStage to select new directories
        mainStage.hide();
        selectorStage.show();

    }

    private void editEntry() {

        //Rebuild Backend
        editor.editEntryInConfig(valueField.getText(), commentField.getText(), configManager);

        //Rebuild Frontend
        changeEditingEntry(editor.getItem(), configManager.getFullConfig());
    }

    private void requestDelete() {

        //Confirm before continue removing the element
        if (showAlert("Confirmation", Value.DeleteConfirmation,
                Value.DeleteConfirmation(editor.getPath()), Alert.AlertType.CONFIRMATION)) {

            //Remove item in Backend (both config variables)
            editor.deleteSelectedEntry(configManager);

            //Remove item in Frontend (visual TreeView)
            tree.remove(editor.getItem());
        }
    }

    private void requestRename() {
        TextInputDialog dialog = new TextInputDialog();
        setTexts(dialog, Value.RenameKeyTitle, Value.RenameKeyHeader, Value.RenameKeyContent);
        String newName = dialog.showAndWait().orElse(null);
        if (newName == null || newName.isEmpty()) return;

        ConfigValue cutFull = configManager.getFullConfig().resolve().getValue(editor.getPath());
        Config restFull = configManager.getFullConfig().withoutPath(editor.getPath());
        ConfigValue cutApplication = configManager.getApplicationConfig().resolve().getValue(editor.getPath());
        Config restApplication = configManager.getApplicationConfig().withoutPath(editor.getPath());

        List<String> oldPath = ConfigUtil.splitPath(editor.getPath());
        oldPath.remove(oldPath.size() - 1);
        oldPath.add(newName);
        String path = ConfigUtil.joinPath(oldPath);

        Config newFullConfig = restFull.withValue(path, cutFull);
        Config newApplicationConfig = restApplication.withValue(path, cutApplication);

        configManager.setFullConfig(newFullConfig);
        configManager.setApplicationConfig(newApplicationConfig);

        editor.getItem().setValue(newName);
    }

    private void requestNewKey() {

        Dialog dialog = createNewKeyDialog();
        Optional result = dialog.showAndWait();
        result.ifPresent(
                arrayObj -> {
                    String[] array = (String[]) arrayObj;
                    createNewKey(array[0], array[1], array[2]);
                });

        //0 -> Key
        //1 -> Type
        //2 -> Value

    }

    private Dialog createNewKeyDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        setTexts(dialog, "Add key", "Select a key path, a value and a value type!", "");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 100, 10, 10));
        TextField key = new TextField();
        key.setPromptText("Key");
        TextField value = new TextField();
        value.setPromptText("Value");
        ChoiceBox<String> type = new ChoiceBox<>(FXCollections.observableArrayList(
                "STRING", "NUMBER", "BOOLEAN"
        ));
        type.setValue("STRING");

        grid.add(new Label(Value.EnterKeyTitle), 0, 0);
        grid.add(key, 1, 0);
        grid.add(new Label(Value.EnterTypeTitle), 0, 1);
        grid.add(type, 1, 1);
        grid.add(new Label(Value.EnterValueTitle), 0, 2);
        grid.add(value, 1, 2);

        // Enable/Disable ok button depending on whether a key was entered.
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        key.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        //Focus key field
        Platform.runLater(key::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new String[]{key.getText(), type.getValue(), value.getText()};
            }
            return null;
        });

        return dialog;
    }

    private void createNewKey(String key, String type, String value) {
        Object finalValue = null;
        try {
            switch (type) {
                case "STRING":
                    finalValue = value;
                    break;
                case "NUMBER":
                    finalValue = Double.parseDouble(value);
                    break;
                case "BOOLEAN":
                    finalValue = Boolean.parseBoolean(value);
                    break;
            }
        } catch (NumberFormatException e) {
            showAlert("Error", Value.ConversionError, Value.TypeConversionError("number"), Alert.AlertType.ERROR);
            return;
        }

        ConfigValue configValue = ConfigValueFactory.fromAnyRef(finalValue, "Added by HOCON Viewer");

        //Add selected path to main configs
        Config newFullConf = configManager.getFullConfig().withValue(key, configValue);
        Config newApplicationConf = configManager.getApplicationConfig().withValue(key, configValue);

        //Apply changes to main configs
        configManager.setFullConfig(newFullConf);
        configManager.setApplicationConfig(newApplicationConf);

        tree.rebuild(newFullConf);
    }

    private static void setTexts(Dialog dialog, String title, String header, String content) {
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
    }


    public static boolean showAlert(String title, String header, String content, Alert.AlertType type) {

        //Show alert for warnings, problems, errors (specified by type)
        Alert alert = new Alert(type);
        setTexts(alert, title, header, content);
        Optional<ButtonType> result = alert.showAndWait();
        return !result.isPresent() || result.get() == ButtonType.OK;
    }

}
