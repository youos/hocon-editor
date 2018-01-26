package com.youos.hoconeditor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.youos.hoconeditor.editor.EditorUI;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

/**
 * Class ConfigManager:
 *
 * Responsible for building and managing Config data
 * Also contains some useful static functions
 */

public class ConfigManager {

    private ArrayList<Config> configs = new ArrayList<>();

    private Config applicationConfig;
    private Config fullConfig;

    private String applicationFile;

    private boolean ready = false;

    /**
     * Starts building Config variables
     * @param directions ArrayList containing every direction for .conf files filled by the user
     */
    public ConfigManager(ArrayList<Path> directions){
        for (Path path : directions) buildConfigs(path);
        createFinal();
    }

    public Config getFullConfig(){return fullConfig.resolve();}

    public Config getApplicationConfig(){return applicationConfig.resolve();}

    public void setFullConfig(Config config){
        fullConfig = config.resolve();
    }
    public void setApplicationConfig(Config config){
        applicationConfig = config.resolve();
    }

    public boolean isReady(){
        return ready;
    }


    /**
     * Creates Config objects from parsing every application.conf and reference.conf found in the folder
     * and adds them to global ArrayList
     * @param dir direction for folder containing .conf and .jar files
     */
    private void buildConfigs(Path dir){

        //Iterate through folder
        for (File file : Objects.requireNonNull(new File(dir.toString()).listFiles())) {

            //Get file extension
            String ext = getExtension(file);

            //.jar -->
            if (ext.equals("jar")) configs.add(parseJar(file));

            //.conf -->
            else{
                try {
                    if (file.getName().equals("application.conf") || file.getName().equals("reference.conf")){
                        configs.add(ConfigFactory.parseFile(file));
                    }
                } catch (ConfigException | NullPointerException ignored){}
            }

        }

        //Remove all configs that have no usable value (happens if jar parsing fails)
        configs.removeAll(Collections.singleton(null));
        for(int i = 0; i < configs.size(); i++){
            if (configs.get(i).isEmpty()) configs.remove(configs.get(i));
        }
    }

    /**
     * Searches in jar for reference.conf and parses it into a single Config object
     * @param jarFile File containing full path to .jar file
     * @return Config from reference.conf in jar
     */
    private Config parseJar(File jarFile) {
        try {
            URL url = new URL("jar:file:" + jarFile.getAbsolutePath() + "!/reference.conf");
            return ConfigFactory.parseURL(url);
        } catch (MalformedURLException ignored) {
            return null;
        }
    }

    /**
     * Merges all configurations on ArrayList configs together,
     * ensuring that application.conf "wins" over reference.conf.
     *
     * Creates global variables applicationConfig, applicationFile and fullConfig
     */
    private void createFinal(){

        //Move application.conf to the end of the list
        configs.sort(new ConfigComparison());

        //If there are 2 or more configs with origin: application.conf --> showAlert and return
        if (getApplicationCount() > 1){
            EditorUI.showAlert("Information", null, "There are " + getApplicationCount() + " application.conf files " +
                    "in your selected folders. Please ensure that there is only one application.conf!", Alert.AlertType.INFORMATION);
            ready = false;
            return;
        }

        //Copy applicationConfig and applicationFile into variables
        applicationConfig = configs.get(configs.size() - 1).resolve();
        applicationFile = applicationConfig.origin().filename();

        //Start merging (needs starting point)
        Config finalConfig = configs.get(0);
        for (Config conf : configs) {

            //Merging with fallback, so that the index higher config "wins"
            try{finalConfig = configs.get(configs.indexOf(conf) + 1).resolve().withFallback(finalConfig);}
            catch(java.lang.IndexOutOfBoundsException ignored){}

            //Error means unresolvable configuration, so there is an error with resolving a value
            catch(ConfigException e){
                Config config = configs.get(configs.indexOf(conf) + 1);
                EditorUI.showAlert("Error", null, "Your file " + config.origin().filename() + " has substitution problems!", Alert.AlertType.ERROR);
                ready = false;
                return;
            }
        }

        //Resolve final configuration to global fullConfig
        fullConfig = finalConfig.resolve();
        ready = true;
    }

    class ConfigComparison implements Comparator<Config> {

        /**
         * Sorts a List so that Configs with origin application.conf move to the end
         * @param c1 Config with index one lower than c2
         * @param c2 Config with index one higher than c1
         * @return number of moves to go through the index for c1
         */
        @Override
        public int compare(Config c1, Config c2) {
            String file1 = new File(c1.origin().description()).getName();
            String file2 = new File(c2.origin().description()).getName();
            if(file1.contains("application.conf")) return 1;
            if(file1.equals(file2)) return 0;
            return -1;
        }
    }


    /**
     *
     * @param file represents a File object with file path
     * @return only extension of filename (for example "conf")
     */
    private String getExtension(File file){

        String extension = "";
        int i = file.getName().lastIndexOf(".");
        if (i > 0) {
            extension = file.getName().substring(i + 1);
        }
        return extension;
    }


    /**
     *
     * @return current count of configs with origin: application.conf
     */
    public int getApplicationCount(){

        int count = 0;
        for (Config c : configs){
            try{
                if (new File(c.origin().filename()).getName().equals("application.conf")) count++;
            } catch (NullPointerException ignored){}

        }
        return count;
    }

    /**
     * Overwrites the existing application.conf with the content of the current applicationConf
     * containing only data that was changed during process or that already existed in file.
     */
    public void saveDataToFile() {

        //Get a valid config string
        ConfigRenderOptions options = ConfigRenderOptions.defaults().setOriginComments(false).setFormatted(true).setComments(true).setJson(false);
        String newConfRendered = getApplicationConfig().root().render(options);

        //Remove useless line breaks
        newConfRendered = newConfRendered.replace("\n\n", "\n");

        //Prepare writer
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(applicationFile, "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //Write content to file and close it
        Objects.requireNonNull(writer).print(newConfRendered);
        Objects.requireNonNull(writer).close();
    }

    /**
     *
     * @param fileDescription String to be analyzed
     * @param keepEdited boolean decides whether "(Edited) " phrase should be kept in or not
     * @return filepath without line number and maybe without edited phrase
     */
    public static String rawFileString(String fileDescription, boolean keepEdited){

        int startIndex = 0;
        if (!keepEdited){
            String edited = "(Edited) ";
            startIndex = fileDescription.indexOf(edited);
            startIndex = startIndex == -1 ? 0 : startIndex + edited.length();
        }
        int endIndex = fileDescription.lastIndexOf(":");
        return fileDescription.substring(startIndex, endIndex);
    }
}
