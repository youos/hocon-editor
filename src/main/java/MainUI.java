import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
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
import java.util.Map;


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

/*

0. Sortierung --> Klappt, application.conf wird nun zuletzt auf die anderen reference.conf geladen
1. Kommentare --> Klappt, Müssen immer VOR einem Key-Value Paar stehen (Zeilen drüber geht auch) bei dem Value kein weiterer Ast ist
                  Priorisierung von reference.conf Kommentaren schwierig
2. Scroll-Areas --> Klappt, vertikale ScrollBar verschwindet bei fileField nicht (eventuell CSS versuchen)
3: Nur eine application.conf --> Frage: Falls zu viele, welche soll rausgeschmissen werden bzw. nur Fehlermeldung? Anzeigen? Neuer Baum?
4. 2 Konfigs --> Klappt (es existieren nun die Variablen fullConfig und applicationConfig)
7. Eintrag löschen --> Frontend steht
 */


class MainUI {

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

    private Loader loader;

    private TreeView<String> buildTree(){
        return new TreeBuilder(loader.getFullConfig(), this, loader).getTreeView();
    }

    MainUI(ArrayList<Path> dir, Stage selectorStage){
        this.selectorStage = selectorStage;
        this.loader = new Loader(dir);

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
        editPane.add(pathLabel, 0, 0);
        editPane.add(pathField, 1, 0);
        editPane.add(valueLabel, 0, 1);
        editPane.add(valueField, 1, 1);
        editPane.add(editBtn, 2, 1);
        editPane.add(typeLabel, 0, 2);
        editPane.add(typeField, 1, 2);
        editPane.add(fileLabel, 0, 3);
        editPane.add(fileField, 1, 3);
        editPane.add(commentLabel, 0, 4);
        editPane.add(commentField, 1, 4);

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
            StringBuilder comment = new StringBuilder();
            for (String c : value.origin().comments()) comment.append(c).append("; ");
            valueField.setText(value.render());
            typeField.setText(value.valueType().name());
            commentField.setText(comment.toString());
            editBtn.setDisable(false);
        } else {
            valueField.clear();
            commentField.clear();
            typeField.setText("PATH");
            valueField.setDisable(true);
            editBtn.setDisable(true);
        }
        editBtn.setText("EDIT");
        pathField.setText(path.toString());
        fileField.setText(file);
    }

    private void deleteSelectedEntry(){
        //TODO Delete selected Entry from Tree
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
                Map<String, Object> newFullConf = loader.getFullConfig().unwrapped();
                newFullConf.put(pathField.getText(), ConfigValueFactory.fromAnyRef(valueField.getText())); //TODO Create Parsable Map
                loader.setFullConfig(ConfigFactory.parseMap(newFullConf).root());
                tree = buildTree();
            }
        });
        openBtn.setOnAction(event -> selectNewFolders());
        saveBtn.setOnAction(event -> saveDataToFiles());
        deleteBtn.setOnAction(event -> deleteSelectedEntry());
    }

    private void saveDataToFiles(){
        //TODO Write TreeView Content into application.conf
    }

}
