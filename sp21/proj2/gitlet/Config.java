package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;

import static gitlet.Repo.REPO;

/**
 * Represent the config of the gitlet repo, unlike Index,
 * manually saving is not needed
 */
public class Config implements Serializable {
    private final TreeMap<String, File> remotes = new TreeMap<>();

    // TODO: this class may be redundant, may be integrate into filesystem
    public static Config fromFile() {
        return Utils.readObject(REPO.configPath(), Config.class);
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
        remotes.put(name, path);
        save();
    }

    /**
     * Remove the record of the remote repo
     */
    public void rmRemote(String name) {
        remotes.remove(name);
        save();
        REPO.rmRemoteDir(name);
    }

    /**
     * Return whether the remote is recorded
     */
    public boolean isRemote(String name) {
        return remotes.containsKey(name);
    }

    /**
     * Get the path of a remote repo, null if not recorded
     */
    public File getPath(String name) {
        return remotes.get(name);
    }

    private void save() {
        Utils.writeObject(REPO.configPath(), this);
    }
}
