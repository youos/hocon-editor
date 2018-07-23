package com.youos.hoconeditor.editor;

import com.typesafe.config.*;
import com.youos.hoconeditor.ConfigManager;
import com.youos.hoconeditor.Value;
import javafx.scene.control.TreeItem;

import java.util.List;

class Editor {

    private ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults().setComments(false).setOriginComments(false).setFormatted(false);

    private TreeItem<String> item;

    TreeItem<String> getItem() {
        return item;
    }

    private String editFile;
    private String editPath;
    private List<String> editComments;
    private String editType;
    private String editValue;
    private String editEnvironment;
    private Boolean editDisable;

    Boolean getBtnDisabled() {
        return editDisable;
    }

    String getFile() {
        return editFile;
    }

    String getPath() {
        return editPath;
    }

    String getType() {
        return editType;
    }

    String getValue() {
        return editValue;
    }

    String getEnvironment(){return editEnvironment; }

    String getComment() {
        StringBuilder sb = new StringBuilder();
        for (String comment : editComments){
            sb.append(comment);
            sb.append("\n");
        }
        return sb.toString().substring(0, sb.length() - 1);
    }

    Editor(){}

    void setup(TreeItem<String> item, Config config){
        if (item == null) return;
        this.item = item;

        //Build path string separated by dots
        StringBuilder path = new StringBuilder();
        for (TreeItem<String> editItem = item; editItem.getParent() != null; editItem = editItem.getParent()){
            String dot = path.toString().equals("") ? "" : ".";
            path.insert(0, editItem.getValue() + dot);
        }

        editPath = path.toString();
        ConfigValue value = config.getValue(editPath);
        editFile = ConfigManager.RawFileString(value.origin().description(), true);
        editComments = value.origin().comments();

        if (item.isLeaf()){
            editValue = value.render(renderOptions);
            editType = value.valueType().name();
            editEnvironment = value.origin().substitutionPath();
            editDisable = false;
        } else {
            editValue = "";
            editType = "PATH";
            editEnvironment = "";
            editDisable = true;
        }
    }

    void editEntryInConfig(ConfigManager manager){

        String env = editEnvironment;
        String comment = getComment();
        String value = editValue;
        String commentString = comment.isEmpty() ? "" : "#" + comment + "\n";
        String configString =  commentString + editPath + "=" + (!env.isEmpty() ? "${" + env + "}" : value);


        //Determine if fileField needs "(edited)" phrase
        String edited = Value.Edited;
        String oldValue = manager.getFullConfig().getValue(editPath).render(renderOptions);
        List<String> oldComments = manager.getFullConfig().getValue(editPath).origin().comments();
        if (oldValue.equals(value) && oldComments.equals(editComments) || editFile.contains(edited)) edited = "";

        //Parsing String to new Config and merge it with both main Configs while this Config will "win"
        ConfigParseOptions parseOptions = ConfigParseOptions.defaults().setOriginDescription(edited + editFile);
        Config addConf = ConfigFactory.parseString(configString, parseOptions);
        Config newFullConf = addConf.withFallback(manager.getFullConfig());
        Config newApplicationConf = addConf.withFallback(manager.getApplicationConfig());

        //Apply changes to main configs
        manager.setFullConfig(newFullConf);
        manager.setApplicationConfig(newApplicationConf);
    }

    void deleteSelectedEntry(ConfigManager manager){
        String path = getPath();

        //Remove selected path from main configs
        Config newFullConf = manager.getFullConfig().withoutPath(path);
        Config newApplicationConf = manager.getApplicationConfig().withoutPath(path);

        //Apply changes to main configs
        manager.setFullConfig(newFullConf);
        manager.setApplicationConfig(newApplicationConf);
    }

}
