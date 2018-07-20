package com.youos.hoconeditor.editor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigValue;
import com.youos.hoconeditor.ConfigManager;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.Map;

class Tree {

    private TreeView<String> tree;

    Tree(EditorUI UI, ConfigManager configManager){

        TreeItem<String> root = new TreeItem<> ("Configuration");

        tree = new TreeView<>(root);
        tree.setMaxWidth(350);
        tree.setShowRoot(false);
        tree.getSelectionModel().selectedItemProperty().addListener((observable, oldVal, newVal) ->
                UI.changeEditingEntry(newVal, configManager.getFullConfig())
        );

        build(configManager.getFullConfig(), root);

    }

    private void build(Config config, TreeItem<String> root){

        for (Map.Entry<String, ConfigValue> stringConfigValueEntry : config.root().entrySet()) {

            String key = (String) ((Map.Entry) stringConfigValueEntry).getKey();
            TreeItem<String> leaf = new TreeItem<>(key);
            root.getChildren().add(leaf);


            try{
                if (config.root().get(key).valueType().name().equals("OBJECT")) {
                    System.out.println(config.root().get(key).origin().substitutionPath());
                    build(config.getObject(key).toConfig(), leaf);
                }
            } catch (ConfigException ignored){}
        }
    }

    public void rebuild(Config config){
        TreeItem<String> root = tree.getRoot();
        root.getChildren().remove(0, root.getChildren().size());
        build(config, root);
    }

    void remove(TreeItem<String> treeItem){
        checkTree(treeItem, tree.getRoot());
    }

    private void checkTree(TreeItem<String> itemToRemove, TreeItem<String> root){
        for (TreeItem<String> item : root.getChildren()){
            if (item.equals(itemToRemove)){
                root.getChildren().remove(item);
                break;
            } else {
                checkTree(itemToRemove, item);
            }
        }
    }


    TreeView<String> getTreeView(){
        return tree;
    }

}
