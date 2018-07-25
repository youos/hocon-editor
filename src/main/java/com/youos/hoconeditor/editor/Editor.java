package com.youos.hoconeditor.editor;

import com.typesafe.config.*;
import com.youos.hoconeditor.ConfigManager;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.util.*;

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
    private Boolean editDisabled;

    boolean getBtnDisabled() {
        return editDisabled;
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

    List<String> getComments(){
        return editComments;
    }

    Editor(){}

    void readProperties(TreeItem<String> item, Config config){
        if (item == null) return;
        this.item = item;

        //Build path string separated by dots
        StringBuilder path = new StringBuilder();
        for (TreeItem<String> editItem = item; editItem.getParent() != null; editItem = editItem.getParent()){
            String dot = path.toString().equals("") ? "" : ".";
            path.insert(0, editItem.getValue() + dot);
        }

        boolean fromEnv = false;
        editPath = path.toString();
        ConfigValue value = config.getValue(editPath);
        ConfigOrigin origin = value.origin();
        editComments = origin.comments();
        editFile = ConfigManager.RawFileString(origin.description());
        if (editFile.equals("env variables")) {
            fromEnv = true;
        }

        if (item.isLeaf()){
            editValue = value.render(renderOptions);
            editType = value.valueType().name();
            editEnvironment = fromEnv ? origin.substitutionPath() : "";
            editDisabled = false;
        } else {
            editValue = "";
            editType = "PATH";
            editEnvironment = "";
            editDisabled = true;
        }
    }

    void editEntryInConfig(ConfigManager manager, ObservableList<CharSequence> commentRaw, String valueRaw, String envVarRaw){

        String comment = parseComments(commentRaw, true);
        String value;
        String configString;
        if (envVarRaw == null || envVarRaw.isEmpty()){
            value = valueRaw;
        } else {
            value = "${" + envVarRaw + "}";
        }
        configString = comment + editPath + "=" + (value.isEmpty() ? "default value" : value);

        //Parsing String to new Config and merge it with both main Configs while this Config will "win"
        Config addConf = ConfigFactory.parseString(configString);
        try{
            Config newFullConf = addConf.withFallback(manager.getFullConfig()).resolve();
            Config newApplicationConf = addConf.withFallback(manager.getApplicationConfig()).resolve();

            //Apply changes to main configs
            manager.setFullConfig(newFullConf);
            manager.setApplicationConfig(newApplicationConf);
        } catch (ConfigException.UnresolvedSubstitution e){
            //TODO Error Message Environment variable is missing
        }
    }

    void deleteSelectedEntry(ConfigManager manager){

        //Remove selected path from main configs
        Config newFullConf = manager.getFullConfig().withoutPath(editPath);
        Config newApplicationConf = manager.getApplicationConfig().withoutPath(editPath);

        //Apply changes to main configs
        manager.setFullConfig(newFullConf);
        manager.setApplicationConfig(newApplicationConf);
    }

    static String parseComments(List<String> comments, boolean withSyntax) {
        StringBuilder sb = new StringBuilder();
        for (String comment : comments){
            if (withSyntax) sb.append("#");
            sb.append(comment);
            sb.append("\n");
        }
        if (sb.length() < 2) return "";
        return sb.toString();
    }

    static String parseComments(ObservableList<CharSequence> comments, boolean withSyntax){
        List<String> newComments = new ArrayList<String>(comments.size());
        for (CharSequence cs : comments){
            newComments.add(cs.toString());
        }
        return parseComments(newComments, withSyntax);
    }

}
