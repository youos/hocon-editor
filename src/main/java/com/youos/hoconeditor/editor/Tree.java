package com.youos.hoconeditor.editor;

import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.youos.hoconeditor.ConfigManager;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.Map;

public class Tree {

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

    private void build(ConfigObject config, TreeItem<String> root){

        for (Map.Entry<String, ConfigValue> stringConfigValueEntry : config.entrySet()) {
            try {
                String key = (String) ((Map.Entry) stringConfigValueEntry).getKey();
                TreeItem<String> leaf = new TreeItem<>(key);
                root.getChildren().add(leaf);

                if (config.toConfig().getValue(key).valueType().name().equals("OBJECT")) {
                    build(config.toConfig().getObject(key), leaf);
                }

            } catch (Exception ignored) {
            }

        }
    }

    public TreeView<String> getTreeView(){
        return tree;
    }

}
