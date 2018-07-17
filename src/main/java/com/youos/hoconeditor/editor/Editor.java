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
    private String editComment;
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

    String getComment() {
        return editComment;
    }

    String getType() {
        return editType;
    }

    String getValue() {
        return editValue;
    }

    String getEnvironment(){return editEnvironment; }

    Editor(){}

    void setup(TreeItem<String> item, Config config){
        this.item = item;

        //Build path string separated by dots
        StringBuilder path = new StringBuilder();
        for (TreeItem<String> editItem = item; editItem.getParent() != null; editItem = editItem.getParent()){
            String dot = path.toString().equals("") ? "" : ".";
            path.insert(0, editItem.getValue() + dot);
        }


        editPath = path.toString();
        Config resolved = config.resolve();
        ConfigValue value = resolved.getValue(editPath);
        editFile = ConfigManager.RawFileString(value.origin().description(), true);


        if (item.isLeaf()){
            List<String> comments = value.origin().comments();
            editValue = value.render(renderOptions);
            editType = value.valueType().name();
            editComment = comments.size() > 0 ? comments.get(0) : "";
            //editEnvironment =
            editDisable = false;
        } else {
            editValue = "";
            editComment = "";
            editType = "PATH";
            editEnvironment = "";
            editDisable = true;
        }
    }

    void editEntryInConfig(String value, String comment, ConfigManager manager){

        String commentString = comment.isEmpty() ? "" : "#" + comment + "\n";
        String configString =  commentString + editPath + "=" + value;

        //Determine if fileField needs "(edited)" phrase
        String edited = Value.Edited;
        String oldValue = manager.getFullConfig().getValue(editPath).render(renderOptions);
        List<String> oldComments = manager.getFullConfig().getValue(editPath).origin().comments();
        String oldComment = oldComments.size() > 0 ? oldComments.get(0) : "";
        if (oldValue.equals(value) && oldComment.equals(comment) || editFile.contains(edited)) edited = "";

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
