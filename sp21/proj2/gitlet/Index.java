package gitlet;

import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

import static gitlet.Utils.*;
/**
 * Represent the content inside index file(staging area)
 */
public class Index implements Serializable {
    // TODO: which should private and if it is necessary to write methods
    // that make manipulate index easier
    /** Staging area for add containing (name, hash) for blobs. */
    public TreeMap<String, String> indexAdd = new TreeMap<>();
    /** Staging area for rm containing name for blobs. */
    public TreeSet<String> indexRm = new TreeSet<>();

    public static Index fromFile() {
        return readObject(Repository.INDEX_FI, Index.class);
    }

    /**
     * Create or overwrite the staging area
     */
    // TODO: will overwrite it inefficient?
    public void saveIndex() {
        writeObject(Repository.INDEX_FI, this);
    }
}
