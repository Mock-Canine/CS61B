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
    // TODO: figure out which field need to be set private
    /** The message of this Commit. */
    private final String message;
    /** The timestamp of this commit. */
    private final Date date;
    // TODO: handle multiple parents here
    /** The parents of commit. */
//    public transient Commit parent;
    private String parentHash;
    /** the branch of the commit */
    private String branch;
    /** The blobs tracked by the commit */
    private TreeMap<String, String> blobs;

    /**
     * Retrieve a commit object from file
     * @param name "HEAD" or valid commit hash
     */
    // TODO: Pay attention to invalid hash situation or typo of HEAD(HED for example)
    public static Commit fromFile(String name) {
        String headHash = name;
        if (name.equals("HEAD")) {
            // Return the branch name under refs/heads/ HEAD points to
            String branch = readContentsAsString(Repository.HEAD_FI);
            // Return the branch hash
            File head = join(Repository.HEADS_DIR, branch);
            headHash = readContentsAsString(head);
        }
        // Retrieve it from objects/commits/headHash
        File commit = join(Repository.COMMITS_DIR, headHash);
        // FIXME: When read back, the transient part will be set to default,
        // so need to give the proper value
        return readObject(commit, Commit.class);
    }

    public Commit(String message) {
        this.message = message;
        // If no branch inside heads/, treat as initial commit
        if (plainFilenamesIn(Repository.HEADS_DIR).isEmpty()) {
            parentHash = "";
            blobs = new TreeMap<>();
            date = Date.from(Instant.EPOCH);
            branch = "master";
        } else {
            // TODO: may be should cache the parent and index(just invoke fromFile() once and cache)
            Commit parent = fromFile("HEAD");
            // TODO: may be better approach
            parentHash = sha1((Object) serialize(parent));
            blobs = parent.blobs;
            // Update blobs
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
    }

    /**
     * Update commits/, refs/heads/
     */
    public void saveCommit() {
        byte[] serialized = serialize(this);
        String hash = sha1((Object) serialized);
        // Use hash as the file name
        File content = join(Repository.COMMITS_DIR, hash);
        writeContents(content, (Object) serialized);
        // Create or overwrite the branch pointer
        File head = join(Repository.HEADS_DIR, branch);
        writeContents(head, hash);
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
}
