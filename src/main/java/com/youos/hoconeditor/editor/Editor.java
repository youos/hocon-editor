package com.youos.hoconeditor.editor;

import com.typesafe.config.*;
import com.youos.hoconeditor.ConfigManager;
import javafx.scene.control.TreeItem;

import java.util.List;

class Editor {

    private ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults().setComments(false).setOriginComments(false).setFormatted(false);

    private TreeItem<String> item;

    public TreeItem<String> getItem() {
        return item;
    }

    private String editFile;
    private String editPath;
    private String editComment;
    private String editType;
    private String editValue;
    private Boolean editDisable;

    public Boolean getBtnDisabled() {
        return editDisable;
    }

    public String getFile() {
        return editFile;
    }

    public String getPath() {
        return editPath;
    }

    public String getComment() {
        return editComment;
    }

    public String getType() {
        return editType;
    }

    public String getValue() {
        return editValue;
    }

    Editor(){}

    void setup(TreeItem<String> item, ConfigObject config){
        this.item = item;
        TreeItem<String> editItem = item;
        StringBuilder path = new StringBuilder();
        for (; editItem.getParent() != null; editItem = editItem.getParent()){
            String dot = path.toString().equals("") ? "" : ".";
            path.insert(0, editItem.getValue() + dot);
        }
        editPath = path.toString();
        editFile = config.toConfig().getValue(path.toString()).origin().description();
        editFile = editFile.substring(0, editFile.lastIndexOf(":"));

        if (item.isLeaf()){
            ConfigValue value = config.toConfig().getValue(path.toString());
            List<String> comments = value.origin().comments();
            editValue = value.render(renderOptions);
            editType = value.valueType().name();
            editComment = comments.size() > 0 ? comments.get(0) : "";
            editDisable = false;
        } else {
            editValue = "";
            editComment = "";
            editType = "PATH";
            editDisable = true;
        }
    }

    void editEntryInConfig(String value, String comment, ConfigManager manager){
        String configString = "#" + comment + "\n" + editPath + "=" + value;
        String changed = "(Edited) ";
        String oldValue = manager.getFullConfig().toConfig().getValue(editPath).render(renderOptions);
        List<String> oldComments = manager.getFullConfig().toConfig().getValue(editPath).origin().comments();
        String oldComment = oldComments.size() > 0 ? oldComments.get(0) : "";
        if (oldValue.equals(value) && oldComment.equals(comment) || editFile.contains(changed)) changed = "";
        ConfigParseOptions parseOptions = ConfigParseOptions.defaults().setOriginDescription(changed + editFile);
        Config addConf = ConfigFactory.parseString(configString, parseOptions);

        //Apply changes to main configs
        manager.setFullConfig(addConf.root());
        manager.setApplicationConfig(addConf.root());
    }

}
