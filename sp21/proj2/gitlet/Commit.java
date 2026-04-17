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
public class Commit implements Serializable, Comparable<Commit> {
    /** The message of this Commit. */
    private final String message;
    /** The timestamp of this commit. */
    private final Date date;
    /** The parents of commit. Use empty string for missing parents */
    private final String parent1Hash;
    private final String parent2Hash;
    /** The hash of the commit, will be set when retrieved from file or initialized */
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
     * Print history a commit, if there are multiple parents,
     * just print the history of first parent
     * @param hash valid commit hash
     */
    public static void printHistory(String hash) {
        // Hit initial commit's parent
        while (!hash.isEmpty()) {
            Commit commit = fromFile(hash);
            System.out.println(commit);
            hash = commit.parent1Hash;
        }
    }

    /**
     * Constructor for non-merge commit
     */
    public Commit(String msg) {
        this(msg, "");
    }

    /**
     * Constructor for merge commit
     */
    public Commit(String msg, String mergedIn) {
        if (msg.isEmpty()) {
            Abort("Please enter a commit message.");
        }
        message = msg;
        parent2Hash = mergedIn;
        if (!GitletIO.existBranch()) {
            parent1Hash = "";
            blobs = new HashMap<>();
            date = Date.from(Instant.EPOCH);
        } else {
            Commit parent = fromFile(GitletIO.headHash());
            parent1Hash = parent.hash;
            // Cp content, not reference
            blobs = new HashMap<>(parent.blobs);
            Index index = Index.fromFile();
            if (index.isEmpty()) {
                Abort("No changes added to the commit.");
            }
            index.clear(blobs);
            index.saveIndex();
            date = Date.from(Instant.now());
        }
        save();
    }

    /**
     * Return whether the file is tracked by the commit
     */
    public boolean tracked(String fileName) {
        return blobs.containsKey(fileName);
    }

    /**
     * Check whether the file in the CWD is the same as in the commit
     * Assume file exists
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
        String merge = parent2Hash.isEmpty() ? "" : "\nMerge: " + parent1Hash.substring(0, 7)
                + " " + parent2Hash.substring(0, 7);
        return """
            ===
            commit %s%s
            Date: %ta %<tb %<te %<tT %<tY %<tz
            %s
            """.formatted(hash, merge, date, message);
    }

    @Override
    public int compareTo(Commit o) {
        return date.compareTo(o.date);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Commit)) {
            return false;
        }
        Commit other = (Commit) o;
        return hash.equals(other.hash);
    }

    /**
     * Save the commit, update branch pointer and assign the hash attribute
     */
    private void save() {
        byte[] serialized = Utils.serialize(this);
        GitletIO.saveCommit(serialized);
        String hash = Utils.sha1((Object) serialized);
        this.hash = hash;
        GitletIO.updateBranch(GitletIO.head(), hash);
    }
}
