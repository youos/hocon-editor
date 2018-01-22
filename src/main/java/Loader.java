import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class Loader {

    private ArrayList<Config> configs = new ArrayList<Config>();

    Loader(ArrayList<Path> directions){
        for (Path path : directions) prepareConfigs(path);
    }

    public ConfigObject getConfig(){
        if (configs.size() == 0) return null;
        Config finalConfig = createFinal();
        return finalConfig.root();
    }


    //C:\Users\Growthteam\Documents\IdeaProjects\hocontool\src\main\resources

    private void prepareConfigs(Path dir){
        File folder = new File(dir.toString());
        File[] files = folder.listFiles();
        for (File file : Objects.requireNonNull(files)) {

            //application.conf overrides reference.conf
            //changing order here won't change overriding
            configs.add(parseJar(dir.toString(), file, "application.conf"));
            configs.add(parseJar(dir.toString(), file, "reference.conf"));

            try {
                if (file.getName().equals("application.conf") || file.getName().equals("reference.conf")){
                    configs.add(ConfigFactory.parseFile(file));
                }
            } catch (ConfigException | NullPointerException ignored){}
        }

        configs.removeAll(Collections.singleton(null));
    }

    private Config parseJar(String dir, File jarFile, String whichConfig){
        try{
            URL url = new URL("jar:file:" + dir + "/" + jarFile.getName() + "!/" + whichConfig);
            InputStream is = url.openStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            return ConfigFactory.parseReader(in);
        }
        catch(java.io.IOException ignored){
            return null;
        }
    }

    private Config createFinal(){
        Config finalConfig = configs.get(0);
        for (Config conf : configs) {
            try{finalConfig = finalConfig.withFallback(configs.get(configs.indexOf(conf) + 1));}
            catch(java.lang.IndexOutOfBoundsException e){break;}
        }
        return finalConfig;
    }

}
