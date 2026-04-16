package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.Main.Abort;
import static gitlet.GitletIO.CWD;

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
    private final HashMap<String, String> blobs;

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
    public Commit(String msg) {
        if (msg.isEmpty()) {
            Abort("Please enter a commit message.");
        }
        message = msg;
        if (GitletIO.getBranches().isEmpty()) {
            parentHash = "";
            blobs = new HashMap<>();
            date = Date.from(Instant.EPOCH);
        } else {
            Commit parent = fromFile(GitletIO.headHash());
            parentHash = parent.hash;
            blobs = parent.blobs;
            Index index = Index.fromFile();
            if (index.isEmpty()) {
                Abort("No changes added to the commit.");
            }
            index.clear(blobs);
            index.saveIndex();
            date = Date.from(Instant.now());
        }
    }

    /**
     * Return whether the file is tracked by the commit
     */
    public boolean tracked(String fileName) {
        return blobs.containsKey(fileName);
    }

    /**
     * Check whether the file in the CWD is the same as in the commit
     */
    public boolean sameAs(String fileName) {
        File fp = Utils.join(CWD, fileName);
        byte[] content = Utils.readContents(fp);
        String fileHash = sha1((Object) content);
        String blobHash = fileHash(fileName);
        return fileHash.equals(blobHash);
    }

    /**
     * Return hash of the file being tracked, null if not being tracked
     */
    public String fileHash(String fileName) {
        return blobs.get(fileName);
    }

    /**
     * Return the files tracked by the commit
     */
    public Set<String> trackedFiles() {
        return blobs.keySet();
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
