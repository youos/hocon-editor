package com.youos.hoconeditor.selector;

public class Value {

    static String sceneTitle = "Search folders:";
    static String startBtn = "OK";
    static String addBtn = "+";
    static String noDirectoryLabel = "No directory selected";
    static String noDirectoryError = "Please select a directory!";

    static String InvalidDirectory(String path){
        return "The directory \"" + path + "\" does not exist!";
    }

    public static String ApplicationCountError(int count){
        return count + " application.conf files found " +
                "in your selected folders. Please ensure that there is exactly one application.conf!";
    }

    public static String SubstitutionError(String filename){
        return "Your file " + filename + " has substitution problems!";
    }

}
