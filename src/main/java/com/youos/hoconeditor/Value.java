package com.youos.hoconeditor;

public class Value {

    public static String
            WindowTitle = "HOCON Viewer",
            SceneTitle = "Search folders:",
            StartBtn = "OK",
            AddBtn = "+",
            SelectBtn = "Select",
            RemoveBtn = "-",
            Edited = "(Edited) ",
            NoDirectoryLabel = "No directory selected",
            NoDirectoryError = "Please select a directory!",
            DeleteConfirmation = "Are you sure you want to remove this entry?",
            OpenBtn = "Open New Directory",
            EditBtn = "Edit",
            DeleteBtn = "Delete Entry",
            SaveBtn = "Apply Changes",
            PathLabel = "Path : ",
            ValueLabel = "Value : ",
            TypeLabel = "Type : ",
            FileLabel = "File : ",
            CommentLabel = "Comment : ";

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
