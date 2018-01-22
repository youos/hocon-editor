import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.ArrayList;

class MainUI {

    private Stage selectorStage;

    private Loader loader;

    MainUI(ArrayList<Path> dir, Stage selectorStage){
        this.selectorStage = selectorStage;
        this.loader = new Loader(dir);

        SplitPane splitter = new SplitPane();

        TreeView<String> tree = buildTree();
        tree.setMaxWidth(350);
        splitter.getItems().addAll(tree);



        Scene scene = new Scene(splitter, 700, 800);
        Stage stage = new Stage();
        stage.setTitle(selectorStage.getTitle());
        stage.setScene(scene);
        stage.show();

    }

    private TreeView<String> buildTree(){
        return new TreeBuilder(loader.getConfig()).getTreeView();
    }

    private void selectNewFolders(Stage stage){
        stage.hide();
        selectorStage.show();
    }







}
