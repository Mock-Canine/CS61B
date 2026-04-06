package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.TreeMap;

/**
 * Represents a gitlet commit object.
 */
public class Commit implements Serializable {
    // TODO: figure out which field need to be set private
    /** The message of this Commit. */
    public final String message;
    /** The timestamp of this commit. */
    public final Date date;
    // TODO: handle multiple parents here
    /** The parents of commit. */
    public transient Commit parent;
    public String parentHash;
    /** the branch of the commit */
    public String branch;
    /** The blobs tracked by the commit */
    public TreeMap<String, String> blobs;

    /**
     * Retrieve a commit object from file
     * @param name "HEAD" or valid commit hash
     */
    // TODO: Pay attention to invalid hash situation or typo of HEAD(HED for example)
    public static Commit fromFile(String name) {
        String headHash = name;
        if (name.equals("HEAD")) {
            // Return the branch name under refs/heads/ HEAD points to
            String branch = Utils.readContentsAsString(Repository.HEAD_FI);
            // Return the branch hash
            File head = Utils.join(Repository.HEADS_DIR, branch);
            headHash = Utils.readContentsAsString(head);
        }
        // Retrieve it from objects/commits/headHash
        File commit = Utils.join(Repository.COMMITS_DIR, headHash);
        // FIXME: When read back, the transient part will be set to default,
        // so need to give the proper value
        return Utils.readObject(commit, Commit.class);
    }

    public Commit(String message) {
        this.message = message;
        // If no branch inside heads/, treat as initial commit
        if (Utils.plainFilenamesIn(Repository.HEADS_DIR).isEmpty()) {
            date = Date.from(Instant.EPOCH);
            parentHash = "";
            branch = "master";
            blobs = new TreeMap<>();
        } else {
            date = Date.from(Instant.now());
            Commit parent = fromFile("HEAD");
            // TODO: may be better approach
            parentHash = Utils.sha1(Utils.serialize(parent));
            branch = parent.branch;
            blobs = parent.blobs;
            // Clear Index
            Index index = Index.fromFile();
            blobs.putAll(index.indexAdd);
            blobs.keySet().removeAll(index.indexRm);
        }
    }

    /**
     * Update commits/, refs/heads/
     */
    public void saveCommit() {
        byte[] serialized = Utils.serialize(this);
        String hash = Utils.sha1(serialized);
        // Use hash as the file name
        File content = Utils.join(Repository.COMMITS_DIR, hash);
        Utils.writeContents(content, serialized);
        // Create or overwrite the branch pointer
        File head = Utils.join(Repository.HEADS_DIR, branch);
        Utils.writeContents(head, hash);
    }
}
