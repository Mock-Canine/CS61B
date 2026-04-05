package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

/** Represents a gitlet commit object.
 */
public class Commit implements Serializable {
    // TODO: persistence part: use SHA string to persistence, and maintain
    // a runtime map for (hash, object),
    // Add redundant pointer as private transient ... as the pointer
    // FIXME: When read back, the transient part will be set to default,
    // so need to give the proper value

    /** The message of this Commit. */
    private final String message;
    /** The timestamp of this commit. */
    private final Date date;
    // TODO: handle multiple parents here
    /** The parents of commit. */
    private transient Commit parent;
    private String parentHash;
    /** the branch of the commit */
    // TODO: only serialize necessary part
    private String branch;
    /** The blobs tracked by the commit */
    // TODO

    /**
     *
     */
    public Commit(String message) {
        this.message = message;
        // If no branch inside heads/, treat as initial commit
        if (Utils.plainFilenamesIn(Repository.HEADS_DIR).isEmpty()) {
            date = Date.from(Instant.EPOCH);
            parent = null;
            parentHash = null;
            branch = "master";
        } else {
            date = Date.from(Instant.now());
            // TODO: look up the HEAD file to initialize parent and hash and branch
        }
    }

    /**
     * Update commits/, HEAD, refs/heads/
     */
    public void saveCommit() {
        Object serialized = Utils.serialize(this);
        String hash = Utils.sha1(serialized);
        // Use hash as the file name
        File content = Utils.join(Repository.COMMITS_DIR, hash);
        Utils.writeContents(content, serialized);
        // Create or overwrite the branch pointer
        File head = Utils.join(Repository.HEADS_DIR, branch);
        Utils.writeContents(head, hash);
    }

    /**
     * Retrieve a commit object from file
     */
    public static Commit fromFile(String name) {
        return null;
    }

}
