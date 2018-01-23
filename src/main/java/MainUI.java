import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
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

0. Sortierung
1. Kommentare falls vorhanden anzeigen (was passiert bei merge?), beide? das von reference priorität
2. Scroll-Area um File
3. Sicherstellen, dass es nur eine application.conf gibt, diese merken und irgendwo anzeigen
4. Praktisch zwei Konfigs vorhalten: alle configs zusammen (resultConfig) und die application.conf
5. Beim editieren immer application.conf Konfiguration bearbeiten
6. beim speichern die application.conf speichern
7. Löschen als Kontextmenü im Baum: Eintrag löschen

 */


class MainUI {

    private Stage selectorStage;
    private Stage mainStage;

    private TextField valueField = new TextField();
    private Text pathField = new Text();
    private Text typeField = new Text();
    private TextField fileField = new TextField();

    private Button editBtn = new Button("Edit");
    private Button openBtn = new Button("Open New Directory");
    private Button saveBtn = new Button("Apply Changes");

    private Loader loader;

    MainUI(ArrayList<Path> dir, Stage selectorStage){
        this.selectorStage = selectorStage;
        this.loader = new Loader(dir);

        pathField.setFill(Color.RED);

        valueField.setDisable(true);
        valueField.setMinWidth(300);

        typeField.setFill(Color.GREEN);

        fileField.setMaxWidth(500);
        fileField.setEditable(false);

        editBtn.setDisable(true);
        editBtn.setPrefWidth(100);

        Label pathLabel = new Label("Path : ");
        Label valueLabel = new Label("Value : ");
        Label typeLabel = new Label("Type : ");
        Label fileLabel = new Label("File : ");

        GridPane editPane = new GridPane();
        editPane.setVgap(10);
        editPane.setHgap(10);
        editPane.setAlignment(Pos.TOP_CENTER);
        editPane.setPadding(new Insets(25, 25, 25, 25));
        editPane.add(pathLabel, 0, 0);
        editPane.add(pathField, 1, 0);
        editPane.add(valueLabel, 0, 1);
        editPane.add(valueField, 1, 1);
        editPane.add(editBtn, 2, 1);
        editPane.add(typeLabel, 0, 2);
        editPane.add(typeField, 1, 2);
        editPane.add(fileLabel, 0, 3);
        editPane.add(fileField, 1, 3);

        TreeView<String> tree = buildTree();
        tree.setMaxWidth(350);
        tree.setShowRoot(false);

        prepareEvents();

        ToolBar toolBar = new ToolBar(openBtn, saveBtn);
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

    void changeEditingEntry(TreeItem<String> item, ConfigObject config){
        TreeItem<String> editItem = item;
        StringBuilder path = new StringBuilder();
        for (; editItem.getParent() != null; editItem = editItem.getParent()){
            String dot = path.toString().equals("") ? "" : ".";
            path.insert(0, editItem.getValue() + dot);
        }
        String file = config.toConfig().getValue(path.toString()).origin().description();
        if (item.isLeaf()){
            ConfigValue value = config.toConfig().getValue(path.toString());
            valueField.setText(value.render());
            typeField.setText(value.valueType().name());
            editBtn.setDisable(false);
        } else {
            valueField.clear();
            typeField.setText("PATH");
            valueField.setDisable(true);
            editBtn.setDisable(true);
        }
        editBtn.setText("EDIT");
        pathField.setText(path.toString());
        fileField.setText(file);
    }

    private TreeView<String> buildTree(){
        return new TreeBuilder(loader.getConfig(), this).getTreeView();
    }

    private void selectNewFolders(){
        mainStage.hide();
        selectorStage.show();
    }

    private void prepareEvents(){
        editBtn.setOnAction(event -> {
            if (valueField.isDisabled()){
                valueField.setDisable(false);
                editBtn.setText("OK");
            } else {
                valueField.setDisable(true);
                editBtn.setText("EDIT");
            }
        });
        openBtn.setOnAction(event -> selectNewFolders());
        saveBtn.setOnAction(event -> saveDataToFiles());
    }

    private void saveDataToFiles(){
        System.out.println("saved");
    }

}
