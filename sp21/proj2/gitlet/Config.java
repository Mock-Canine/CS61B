package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;
import static gitlet.Main.abort;

/**
 * Represent the config of the gitlet repo, unlike Index,
 * manually saving is not needed
 */
public class Config implements Serializable {
    private final TreeMap<String, File> remotes = new TreeMap<>();

    public static Config fromFile() {
        return GitletIO.getConfig();
    }

    /**
     * Clear up the config file
     */
    public static void initConfig() {
        Config config = new Config();
        config.save();
    }

    /**
     * Record the remote name and path into config
     */
    public void addRemote(String name, File path) {
        if (remotes.containsKey(name)) {
            abort("A remote with that name already exists.");
        }
        remotes.put(name, path);
        save();
    }

    public void rmRemote(String name) {
        if (!remotes.containsKey(name)) {
            abort("A remote with that name does not exist.");
        }
        remotes.remove(name);
        save();
        GitletIO.rmRemoteDir(name);
    }

    private void save() {
        GitletIO.saveConfig(this);
    }
}
