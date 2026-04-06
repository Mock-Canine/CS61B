package gitlet;

import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Represent the content inside index file(staging area)
 */
public class Index implements Serializable {
    // TODO: which should private
    /** Staging area for add containing (name, hash) for blobs. */
    public TreeMap<String, String> indexAdd = new TreeMap<>();
    /** Staging area for rm containing name for blobs. */
    public TreeSet<String> indexRm = new TreeSet<>();

    public static Index fromFile() {
        return Utils.readObject(Repository.INDEX_FI, Index.class);
    }

    /**
     * Create or overwrite the staging area
     */
    // TODO: will overwrite it inefficient?
    public void saveIndex() {
        Utils.writeObject(Repository.INDEX_FI, this);
    }
}
