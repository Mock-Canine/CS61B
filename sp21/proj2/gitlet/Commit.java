package gitlet;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;
import static gitlet.Main.Abort;

/**
 * Represents a gitlet commit object.
 */
public class Commit implements Serializable {
    /** The message of this Commit. */
    private final String message;
    /** The timestamp of this commit. */
    private final Date date;
    // TODO: handle multiple parents here
    /** The parents of commit. */
    // public transient Commit parent;
    private final String parentHash;
    /** The hash of the commit, will be set when retrieved from file */
    private transient String hash;
    /** The blobs tracked by the commit */
    private final TreeMap<String, String> blobs;

    /**
     * Retrieve a commit object from file
     * @param hash valid format: 4-40 characters long, each represent a
     *             lower case hex number, without any prefix like 0x, 0X, etc.
     * With valid format, it should also indicate a unique commit without ambiguity
     * Abort the program if provide invalid hash
     */
    public static Commit fromFile(String hash) {
        Commit commit = GitletIO.getCommit(hash);
        commit.hash = hash;
        return commit;
    }

    /**
     * Print history a commit, if there are multiple branches,
     * just print the history of current branch and extra merge information
     * @param hash valid commit hash
     */
    public static void printHistory(String hash) {
        // Hit initial commit's parent
        while (!hash.isEmpty()) {
            Commit commit = fromFile(hash);
            System.out.println(commit);
            hash = commit.parentHash;
        }
    }

    /**
     * Save the commit, update branch pointer
     */
    public void save() {
        byte[] serialized = Utils.serialize(this);
        GitletIO.saveCommit(serialized);
        String hash = Utils.sha1((Object) serialized);
        GitletIO.updateBranch(GitletIO.head(), hash);
    }

    /**
     * Create a commit without saving it to filesystem
     */
    public Commit(String message) {
        if (message.isEmpty()) {
            Abort("Please enter a commit message.");
        }
        this.message = message;
        if (GitletIO.branches().isEmpty()) {
            parentHash = "";
            blobs = new TreeMap<>();
            date = Date.from(Instant.EPOCH);
        } else {
            Commit parent = fromFile(GitletIO.headHash());
            parentHash = parent.hash;
            blobs = parent.blobs;
            Index index = Index.fromFile();
            if (index.isEmpty()) {
                Abort("No changes added to the commit.");
            }
            index.updateBlob(blobs);
            index.saveIndex();
            date = Date.from(Instant.now());
        }
    }

    /**
     * Return whether the file tracked by the commit
     */
    public boolean inCommit(String name) {
        return blobs.containsKey(name);
    }

    /**
     * Return hash of the file being tracked, null if not being tracked
     */
    public String blobHash(String name) {
        return blobs.get(name);
    }

    /**
     * Return the files tracked by the commit
     */
    public Set<String> trackedFiles() {
        return blobs.keySet();
    }

    /**
     * Return the hash of this commit
     */
    public String getHash() {
        return hash;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        // TODO: add logic for merge later
        return """
            ===
            commit %s
            Date: %ta %<tb %<te %<tT %<tY %<tz
            %s
            """.formatted(hash, date, message);
    }
}
