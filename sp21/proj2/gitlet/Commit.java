package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.TreeMap;

import static gitlet.Utils.*;

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
//    public transient Commit parent;
    private final String parentHash;
    /** The hash of the commit, will be set when initialized or retrieved from file */
    private transient String myHash;
    /** the branch of the commit */
    private final String branch;
    /** The blobs tracked by the commit */
    private final TreeMap<String, String> blobs;

    /**
     * Retrieve a commit object from file
     * @param hash "HEAD" or valid commit hash
     */
    // TODO: Pay attention to invalid hash situation or typo of HEAD(HED for example)
    public static Commit fromFile(String hash) {
        if (hash.equals("HEAD")) {
            hash = headHash();
        }
        // Retrieve it from objects/commits/
        File name = join(Repository.COMMITS_DIR, hash);
        Commit commit = readObject(name, Commit.class);
        commit.myHash = hash;
        return commit;
    }

    /**
     * Return the hash of the head of current branch
     */
    public static String headHash() {
        // Return the branch name under refs/heads/ HEAD points to
        String branch = readContentsAsString(Repository.HEAD_FI);
        // Return the branch hash
        File head = join(Repository.HEADS_DIR, branch);
        return readContentsAsString(head);
    }

    /**
     * Print history a commit, if there are multiple branches,
     * just print the history of current branch and extra merge information
     * @param hash "HEAD" or valid commit hash
     */
    public static void printHistory(String hash) {
        // TODO: add logic for merge later
        // Hit initial commit's parent
        if (hash.isEmpty()) {
            return;
        }
        Commit commit = fromFile(hash);
        System.out.println(commit);
        printHistory(commit.parentHash);
    }

    /**
     * Create a commit and save it in the filesystem
     */
    public Commit(String message) {
        // If no branch inside heads/, treat as initial commit
        if (plainFilenamesIn(Repository.HEADS_DIR).isEmpty()) {
            parentHash = "";
            blobs = new TreeMap<>();
            date = Date.from(Instant.EPOCH);
            branch = "master";
        } else {
            Commit parent = fromFile("HEAD");
            parentHash = parent.myHash;
            // Update blobs
            blobs = parent.blobs;
            Index index = Index.fromFile();
            if (index.isEmpty()) {
                message("No changes added to the commit.");
                System.exit(0);
            }
            index.updateBlob(blobs);
            index.saveIndex();
            date = Date.from(Instant.now());
            branch = parent.branch;
        }
        this.message = message;
        saveCommit();
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
     * Return the hash of this commit
     */
    public String hash() {
        return myHash;
    }

    @Override
    public String toString() {
        // TODO: add logic for merge later
        return """
            ===
            commit %s
            Date: %ta %<tb %<te %<tT %<tY %<tz
            %s
            
            """.formatted(myHash, date, message);
    }

    /**
     * Save the commit to objects/commits/, update the hash in the
     * refs/heads/branch, and assign the hash field of this commit
     */
    // TODO: this may be set private, as long as there is no mutator for the class
    private void saveCommit() {
        byte[] serialized = serialize(this);
        String hash = sha1((Object) serialized);
        myHash = hash;
        // Use hash as the file name
        File content = join(Repository.COMMITS_DIR, hash);
        writeContents(content, (Object) serialized);
        // Create or overwrite the branch pointer
        File head = join(Repository.HEADS_DIR, branch);
        writeContents(head, hash);
    }

}
