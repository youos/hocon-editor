import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.Iterator;
import java.util.Map;

class TreeBuilder {

    private TreeView<String> finalTree;

    TreeBuilder(final ConfigObject config, final MainUI UI){

        TreeItem<String> root = new TreeItem<> ("Configuration");

        finalTree = new TreeView<>(root);
        finalTree.getSelectionModel().selectedItemProperty().addListener((observable, oldVal, newVal) ->
                UI.changeEditingEntry(newVal, config)
        );

        build(config, root);

    }

    private void build(ConfigObject config, TreeItem<String> root){
        Iterator<Map.Entry<String, ConfigValue>> keys = config.entrySet().iterator();

        while(keys.hasNext()){
            try{
                Map.Entry entry = keys.next();
                String key = (String) entry.getKey();
                TreeItem<String> leaf = new TreeItem<>(key);
                root.getChildren().add(leaf);

                if (config.toConfig().getValue(key).valueType().name().equals("OBJECT")){
                    build(config.toConfig().getObject(key), leaf);
                }

            } catch(Exception ignored){}

        }
    }

    public TreeView<String> getTreeView(){
        return finalTree;
    }

}
