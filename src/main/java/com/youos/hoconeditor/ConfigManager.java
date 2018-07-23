package com.youos.hoconeditor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.youos.hoconeditor.editor.EditorUI;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Class ConfigManager:
 * <p>
 * Responsible for building and managing Config data
 * Also contains some useful static functions
 */

public class ConfigManager {

    private Config applicationConfig;
    private Config fullConfig;

    private String applicationFilePath;

    public Config getFullConfig() {
        return fullConfig;
    }

    public Config getApplicationConfig() {
        return applicationConfig;
    }

    public void setFullConfig(Config config) {
        fullConfig = config;
    }

    public void setApplicationConfig(Config config) {
        applicationConfig = config;
    }


    /**
     * Starts building Config variables
     *
     * @param paths ArrayList containing every direction for .conf files filled by the user
     */
    public ConfigManager(ArrayList<Path> paths, Stage primaryStage) {
        if (checkApplicationCount(paths)) {
            ArrayList<Config> configs = new ArrayList<>();
            buildConfigs(paths, configs);
            createFinal(configs);
            startEdit(primaryStage);
        }
    }

    private boolean checkApplicationCount(ArrayList<Path> paths) {

        int applications = getApplicationCount(paths);

        if (applications != 1) {
            String text = Value.ApplicationCountError(applications);
            EditorUI.showAlert("Information", null, text, Alert.AlertType.INFORMATION);
            return false;
        }

        return true;
    }

    /**
     * Creates Config objects from parsing every application.conf and reference.conf found in the folder
     * and adds them to global ArrayList
     *
     * @param paths direction for folder containing .conf and .jar files
     */
    private void buildConfigs(ArrayList<Path> paths, ArrayList<Config> configs) {

        for (Path path : paths) {

            //Iterate through folder
            File[] folder = Objects.requireNonNull(new File(path.toString()).listFiles());
            for (File file : folder) {
                readFile(file, configs);
            }
        }


        //Remove all configs that have no usable value (happens if jar parsing fails)
        configs.removeAll(Collections.singleton(null));
    }

    private void readFile(File file, ArrayList<Config> configs) {
        if (file.isDirectory()) {
            File[] folder = Objects.requireNonNull(file.listFiles());
            for (File f : folder) {
                readFile(f, configs);
            }
        }

        if (file.isFile()) {
            //Get file extension
            String extension = Extension(file);

            //.jar -->
            if (extension.equals("jar")) {
                configs.addAll(Objects.requireNonNull(parseJar(file)));
            }

            //.conf -->
            if (extension.equals("conf")) {
                try {
                    configs.add(ConfigFactory.parseFile(file));
                } catch (ConfigException | NullPointerException ignored) {
                }
            }
        }
    }

    private void startEdit(Stage primaryStage) {
        //Hide selector window
        primaryStage.hide();

        //Open editor window
        new EditorUI(this, primaryStage);
    }

    /**
     * Searches in jar for reference.conf and parses it into a single Config object
     *
     * @param jarFile File containing full path to .jar file
     * @return Config from reference.conf in jar
     */
    private ArrayList<Config> parseJar(File jarFile) {
        try {
            ArrayList<Config> allConfigs = new ArrayList<>();
            JarFile jar = new JarFile(jarFile.getAbsolutePath());
            Enumeration entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                if (entry.getName().endsWith(".conf")) {
                    URL url = new URL("jar:file:" + jarFile.getAbsolutePath() + "!/" + entry.getName());
                    allConfigs.add(ConfigFactory.parseURL(url));
                }
            }
            return allConfigs;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Merges all configurations on ArrayList configs together,
     * ensuring that application.conf "wins" over reference.conf.
     * <p>
     * Creates global variables applicationConfig, applicationFile and fullConfig
     */
    private void createFinal(ArrayList<Config> configs) {

        //Move application.conf to the end of the list
        configs.sort(new ConfigComparison());

        //Copy applicationConfig and applicationFile into variables
        applicationConfig = configs.get(configs.size() - 1).resolve();
        applicationFilePath = applicationConfig.origin().url().getFile();


        //Start merging all configs (needs starting point)
        Config build = configs.get(0);
        for (Config conf : configs) {
            int index = configs.indexOf(conf);

            //Merging with fallback, so that the index higher config "wins"
            build = index + 1 < configs.size() ? configs.get(index + 1).withFallback(build) : build;
        }

        fullConfig = build.resolve();
    }

    class ConfigComparison implements Comparator<Config> {
        /**
         * Sorts a List so that Configs with origin application.conf move to the end
         *
         * @param c1 Config with index one lower than c2
         * @param c2 Config with index one higher than c1
         * @return number of moves to go through the index for c1
         */
        @Override
        public int compare(Config c1, Config c2) {
            String file1 = new File(c1.origin().description()).getName();
            String file2 = new File(c2.origin().description()).getName();
            if (file1.contains("application.conf")) return 1;
            if (file1.equals(file2)) return 0;
            return -1;
        }
    }

    private int getApplicationCount(ArrayList<Path> paths) {
        int applications = 0;

        for (Path path : paths) {
            for (File file : Objects.requireNonNull(new File(path.toString()).listFiles())) {
                if (file.getName().equals("application.conf")) applications++;
            }
        }

        return applications;
    }

    /**
     * Overwrites the existing application.conf with the content of the current applicationConf
     * containing only data that was changed during process or that already existed in file.
     */
    public void saveDataToFile() {

        //Get a valid config string
        ConfigRenderOptions options = ConfigRenderOptions.defaults().setOriginComments(false).setFormatted(true).setComments(true).setJson(false);
        String newConfRendered = applicationConfig.root().render(options);

        //Remove useless line breaks
        newConfRendered = newConfRendered.replace("\n\n", "\n");

        //Prepare writer
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(applicationFilePath, "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException ignored) {
        }

        //Write content to file and close it
        Objects.requireNonNull(writer).print(newConfRendered);
        Objects.requireNonNull(writer).close();
    }


    /**
     * @param fileDescription String to be analyzed
     * @param keepEdited      boolean decides whether edited phrase should be kept in or not
     * @return filepath without line number and maybe without edited phrase
     */
    public static String RawFileString(String fileDescription, boolean keepEdited) {

        int startIndex = 0;

        if (!keepEdited) {
            startIndex = fileDescription.indexOf(Value.Edited);
            startIndex = startIndex == -1 ? 0 : startIndex + Value.Edited.length();
        }

        int endIndex = fileDescription.lastIndexOf(":");
        if (endIndex == -1) return "";
        return fileDescription.substring(startIndex, endIndex);

    }


    /**
     * @param file represents a File object with file path
     * @return only extension of filename (for example "conf")
     */
    private static String Extension(File file) {

        String extension = "";
        int i = file.getName().lastIndexOf(".");
        if (i > 0) {
            extension = file.getName().substring(i + 1);
        }
        return extension;
    }
}
