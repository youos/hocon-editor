import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class Loader {

    private ArrayList<Config> configs = new ArrayList<>();

    Loader(ArrayList<Path> directions){
        for (Path path : directions) buildConfigs(path);
    }

    public ConfigObject getConfig(){
        if (configs.size() == 0) return null;
        Config finalConfig = createFinal();
        return finalConfig.root();
    }


    private void buildConfigs(Path dir){
        for (File file : Objects.requireNonNull(new File(dir.toString()).listFiles())) {


            String ext = getExtension(file);
            if (ext.equals("jar")){
                //application.conf overrides reference.conf
                //changing order here won't change overriding
                configs.add(parseJar(file, "reference.conf"));
                //configs.add(parseJar(file, "application.conf"));
            }

            try {
                if (file.getName().equals("application.conf") || file.getName().equals("reference.conf")){
                    configs.add(ConfigFactory.parseFile(file));
                }
            } catch (ConfigException | NullPointerException ignored){}
        }

        configs.removeAll(Collections.singleton(null));
    }

    private Config parseJar(File jarFile, String whichConfig) {
        try {
            URL url = new URL("jar:file:" + jarFile.getAbsolutePath() + "!/" + whichConfig);
            return ConfigFactory.parseURL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

    }

    private Config createFinal(){
        configs.sort(new ConfigComparison()); //Move application.conf to the end of the list
        Config finalConfig = configs.get(0);
        for (Config conf : configs) {
            try{finalConfig = finalConfig.withFallback(configs.get(configs.indexOf(conf) + 1));}
            catch(java.lang.IndexOutOfBoundsException e){break;}
        }
        return finalConfig;
    }

    class ConfigComparison implements Comparator<Config> {
        @Override
        public int compare(Config c1, Config c2) {
            String file1 = new File(c1.origin().description()).getName();
            String file2 = new File(c2.origin().description()).getName();
            if(file1.equals("application.conf")) return 1;
            if(file1.equals(file2)) return 0;
            return -1;
        }
    }

    private String getExtension(File file){
        String extension = "";
        int i = file.getName().lastIndexOf('.');
        if (i > 0) {
            extension = file.getName().substring(i + 1);
        }
        return extension;
    }

}
