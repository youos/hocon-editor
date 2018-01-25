package com.youos.hoconeditor;

import com.typesafe.config.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class ConfigManager {

    private ArrayList<Config> configs = new ArrayList<>();
    private Config applicationConfig;
    private Config fullConfig;

    public ConfigManager(ArrayList<Path> directions){
        for (Path path : directions) buildConfigs(path);
        createFinal();
    }

    public ConfigObject getFullConfig(){
        return fullConfig.root();
    }
    public ConfigObject getApplicationConfig(){
        return applicationConfig.root();
    }

    public void setFullConfig(ConfigObject config){
        fullConfig = config.toConfig().withFallback(fullConfig).resolve();
    }
    public void setApplicationConfig(ConfigObject config){
        applicationConfig = config.toConfig().withFallback(applicationConfig).resolve();
    }


    private void buildConfigs(Path dir){
        ConfigParseOptions options = ConfigParseOptions.defaults();
        for (File file : Objects.requireNonNull(new File(dir.toString()).listFiles())) {
            String ext = getExtension(file);
            if (ext.equals("jar")) configs.add(parseJar(file));
            else{
                try {
                    if (file.getName().equals("application.conf") || file.getName().equals("reference.conf")){
                        configs.add(ConfigFactory.parseFile(file, options));
                    }
                } catch (ConfigException | NullPointerException ignored){}
            }

        }
        configs.removeAll(Collections.singleton(null));

        for(int i = 0; i < configs.size(); i++){
            if (configs.get(i).isEmpty()) configs.remove(configs.get(i));
        }
    }

    private Config parseJar(File jarFile) {
        try {
            URL url = new URL("jar:file:" + jarFile.getAbsolutePath() + "!/reference.conf");
            return ConfigFactory.parseURL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void createFinal(){
        configs.sort(new ConfigComparison()); //Move application.conf to the end of the list
        checkApplicationCount();
        applicationConfig = configs.get(configs.size() - 1); //Copy it into a variable
        Config finalConfig = configs.get(0); //Continue with all files (reference.conf and application.conf)
        for (Config conf : configs) {
            try{finalConfig = configs.get(configs.indexOf(conf) + 1).resolve().withFallback(finalConfig);}
            catch(java.lang.IndexOutOfBoundsException ignored){}
            catch(ConfigException e){
                damagedData(configs.get(configs.indexOf(conf) + 1));
            }
        }
        fullConfig = finalConfig.resolve();
    }

    class ConfigComparison implements Comparator<Config> {
        @Override
        public int compare(Config c1, Config c2) {
            String file1 = new File(c1.origin().description()).getName();
            String file2 = new File(c2.origin().description()).getName();
            if(file1.contains("application.conf")) return 1;
            if(file1.equals(file2)) return 0;
            return -1;
        }
    }

    private void checkApplicationCount(){
        int applicationCount = 0;
        for (Config conf : configs){
            if (new File(conf.origin().description()).getName().contains("application.conf")) applicationCount++;
        }
        if (applicationCount < 2) {
            //TODO show ErrorMessage
        }

    }

    private String getExtension(File file){
        String extension = "";
        int i = file.getName().lastIndexOf(".");
        if (i > 0) {
            extension = file.getName().substring(i + 1);
        }
        return extension;
    }

    private void damagedData(Config config){
        //TODO Message for damaged data in path config.origin()
    }

    public void saveDataToFile(){
        //TODO Write Config into application.conf
    }

}
