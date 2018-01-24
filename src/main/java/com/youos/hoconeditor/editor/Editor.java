package com.youos.hoconeditor.editor;

import com.typesafe.config.*;
import com.youos.hoconeditor.ConfigManager;
import javafx.scene.control.TreeItem;

class Editor {

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
        if (item.isLeaf()){
            ConfigValue value = config.toConfig().getValue(path.toString());
            StringBuilder comment = new StringBuilder();
            for (String c : value.origin().comments()) if (!c.isEmpty()) comment.append(c).append("; ");
            editValue = value.render(ConfigRenderOptions.defaults().setComments(false).setOriginComments(false).setFormatted(false));
            editType = value.valueType().name();
            editComment = comment.toString();
            editDisable = false;
        } else {
            editValue = "";
            editComment = "";
            editType = "PATH";
            editDisable = true;
        }
    }

    void editEntryInConfig(String value, ConfigManager manager){
        String configString = "#" + editComment + "\n" + editPath + "=" + value;
        String changed = editFile.contains("(Changed) ") ? "" : "(Changed) ";
        String path = editPath.substring(0, editPath.lastIndexOf("."));
        String key = editPath.substring(editPath.lastIndexOf(".") + 1);
        String oldValue = manager.getFullConfig().toConfig().getValue(editPath).render(ConfigRenderOptions.defaults().setComments(false).setOriginComments(false).setFormatted(false));
        if (oldValue.equals(value)) changed = "";
        Config addConf = ConfigFactory.parseString(configString, ConfigParseOptions.defaults().setOriginDescription(changed + editFile));
        manager.setFullConfig(addConf.root());
        manager.setApplicationConfig(addConf.root());
    }

}
