package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.sha1;
import static gitlet.GitletIO.CWD;

/**
 * Represent the staging area
 * Caution: every operation except saveIndex() will not reflect the change
 * to index file, manually invoking is needed after all possible operations.
 */
public class Index implements Serializable {
    /** Staging area for add containing (name, hash) for blobs. */
    private final Map<String, String> addition = new HashMap<>();
    /** Staging area for rm containing name for blobs. */
    private final Set<String> removal = new HashSet<>();

    /**
     * Retrieve content from index file
     */
    public static Index fromFile() {
        return GitletIO.getIndex();
    }

    /**
     * Create or overwrite index file
     */
    public void saveIndex() {
        GitletIO.saveIndex(this);
    }

    /**
     * Stage file for addition
     */
    public void stageForAddition(String f) {
        File fp = Utils.join(CWD, f);
        byte[] content = Utils.readContents(fp);
        String hash = sha1((Object) content);
        addition.put(f, hash);
        // TODO: a little beyond the abstraction
        GitletIO.saveBlob(hash, content);
    }

    /**
     * Unstage file for addition
     */
    public void unstageForAddition(String name) {
        addition.remove(name);
    }

    /**
     * Return whether the file has been staged for addition
     */
    public boolean isStaged(String name) {
        return addition.containsKey(name);
    }

    /**
     * Stage file for removal
     */
    public void stageForRemoval(String name) {
        removal.add(name);
    }

    /**
     * Unstage file for removal
     */
    public void unstageForRemoval(String name) {
        removal.remove(name);
    }

    /**
     * Return whether staging area(for addition and removal) is empty
     */
    public boolean isEmpty() {
        return addition.isEmpty() && removal.isEmpty();
    }

    /**
     * Integrate files tracked by staging areas into a commit, and clear staging areas
     * @param blobs (name, hash) blob map in commit, will mutate the map
     */
    public void clear(Map<String, String> blobs) {
        blobs.putAll(addition);
        blobs.keySet().removeAll(removal);
        abandon();
    }

    /**
     * Abandon all the contents in the staging area
     */
    public void abandon() {
        addition.clear();
        removal.clear();
    }

    @Override
    public String toString() {
        List<String> staged = new ArrayList<>(addition.keySet());
        List<String> removed = new ArrayList<>(removal);
        staged.sort(null);
        removed.sort(null);
        String content = "";
        if (!staged.isEmpty()) {
            content += """
                === Staged Files ===
                %s
            
                """.formatted(String.join("\n", staged));
        } else {
            content += """
                === Staged Files ===
            
                """;
        }
        if (!removed.isEmpty()) {
            content += """
                === Removed Files ===
                %s
            
                """.formatted(String.join("\n", removed));
        } else {
            content += """
                === Removed Files ===
            
                """;
        }
        return content;
    }
}
