package com.youos.hoconeditor;

/**
 *  This class is a collection of every frontend Label,
 *  created for having better maintenance of texts shown
 */

public class Value {

    //Static Frontend Text Labels
    public static String
            WindowTitle = "HOCON Viewer",
            SceneTitle = "Search folders:",
            StartBtn = "OK",
            AddBtn = "+",
            SelectBtn = "Select",
            RemoveBtn = "-",
            OpenBtn = "Open New Directory",
            SaveBtn = "Apply Changes",
            DeleteBtn = "Delete Key",
            RenameBtn = "Rename Key",
            NewKeyBtn = "Add key",
            PathLabel = "Path : ",
            ValueLabel = "Value : ",
            TypeLabel = "Type : ",
            FileLabel = "File : ",
            CommentLabel = "Comment : ",
            EnvironmentLabel = "Environment variable : ",
            EditBtn = "Save",
            Edited = "(Edited) ";

    //Static Frontend Dialog Labels
    public static String
            NoDirectoryLabel = "No directory selected",
            NoDirectoryError = "Please select a directory!",
            DeleteConfirmation = "Are you sure you want to remove this entry?",
            EnterKeyTitle = "Enter Key Path",
            EnterTypeTitle = "Enter Type",
            EnterValueTitle = "Enter Value",
            RenameKeyTitle = "Refactor",
            RenameKeyHeader = "Type in the new name of the key!",
            RenameKeyContent = "New name:",
            ConversionError = "Type conversion error";

    public static String TypeConversionError(String type){
        return "Your entered value cannot be converted into a " + type.toLowerCase() + "!";
    }

    public static String InvalidDirectory(String path){
        return "The directory \"" + path + "\" does not exist!";
    }

    public static String DeleteConfirmation(String path){
        return "Key to be removed: \n" + path;
    }

    static String ApplicationCountError(int count){
        return count + " application.conf files found " +
                "in your selected folders. Please ensure that there is exactly one application.conf!";
    }

}
