package gitlet;

import java.io.File;
import java.util.List;
import static gitlet.FileSystem.*;
import static gitlet.Main.Abort;
import static gitlet.Utils.sha1;

// TODO: open some operations to other classes, for example CWD operations
/**
 * Provide IO operations for gitlet repo, hide the structure detail from the other class
 */
// TODO: delete the 1 time usage helper method
public class GitletIO {
    /**
     * Init the gitlet filesystem
     */
    public static void init() {
        if (GITLET.exists()) {
            Abort("A Gitlet version-control system already exists in the current directory.");
        }
        // Create required folders, order of creating matters
        mkdir(GITLET);
        mkdir(OBJECTS);
        mkdir(REFS);
        mkdir(COMMITS);
        mkdir(BLOBS);
        mkdir(HEADS);
        // Create required files
        setHead("master");
        Index index = new Index();
        index.saveIndex();
    }

    /**
     * Check if in a gitlet repo
     */
    public static void isInRepo() {
        if (!GITLET.exists()) {
            Abort("Not in an initialized Gitlet directory.");
        }
    }

    /* IO for commit operations, call the corresponding version in Commit to get and save */
    /**
     * Retrieve a commit object from file
     * @param hash valid format: 4-40 characters long, each represent a
     *             lower case hex number, without any prefix like 0x, 0X, etc.
     * With valid format, it should also indicate a unique commit without ambiguity
     * Abort the program if provide invalid hash
     */
    public static Commit getCommit(String hash) {
        if (parsePath(hash) == null) {
            Abort("No commit with that id exists.");
        }
        // Retrieve it from objects/commits/
        File name = Utils.join(COMMITS, hash);
        return Utils.readObject(name, Commit.class);
    }

    /**
     * Return the 40 character hash if input represents a commit, null otherwise
     * @param hash valid format: 4-40 characters long, each represent a
     *             lower case hex number, without any prefix like 0x, 0X, etc.
     * With valid format, it should also indicate a unique commit without ambiguity
     */
    private static String parsePath(String hash) {
        if (!hash.matches("^[0-9a-f]{4,40}$")) {
            return null;
        }
        int num = 0;
        String fullHash = null;
        for (String f : listFiles(COMMITS)) {
            if (f.startsWith(hash)) {
                num++;
                fullHash = f;
            }
        }
        if (num == 1) {
            return fullHash;
        }
        return null;
    }

    /**
     * Save the serialized object content to filesystem
     * Only need the content, use hash as the name is just the design choice
     */
    public static void saveCommit(byte[] content) {
        String hash = sha1((Object) content);
        File file = Utils.join(COMMITS, hash);
        Utils.writeContents(file, (Object) content);
    }

    /* IO for index operations, call the corresponding version in Index to get and save */
    public static Index getIndex() {
        return Utils.readObject(INDEX, Index.class);
    }

    public static void saveIndex(Index index) {
        Utils.writeObject(INDEX, index);
    }

    /* IO for branch operations */
    /**
     * Update the branch pointer to a commit in the filesystem
     * Create new branch if not exists
     */
    public static void updateBranch(String name, String commitHash) {
        File branch = Utils.join(HEADS, name);
        Utils.writeContents(branch, commitHash);
    }

    /**
     * Set head pointer to the named branch
     * Assume provide valid branch name
     */
    public static void setHead(String branch) {
        Utils.writeContents(HEAD, branch);
    }

    /**
     * Return the hash of the head of current branch
     */
    public static String headHash() {
        String branch = head();
        File head = Utils.join(HEADS, branch);
        return Utils.readContentsAsString(head);
    }

    /**
     * Return the branch name head pointer point to
     */
    public static String head() {
        return Utils.readContentsAsString(HEAD);
    }

    /**
     * Return the branch names of this repo
     */
    public static List<String> branches() {
        return listFiles(HEADS);
    }

    /* IO for blobs and working directory files */
    /**
     * Check whether the file is in the CWD
     */
    public static boolean inCWD(String f) {
        File fp = Utils.join(CWD, f);
        return fp.exists();
    }

    /**
     * Save the file in CWD to blobs
     */
    public static void saveBlob(String fileHash, byte[] content) {
        // TODO: a little redundant
        File blob = Utils.join(BLOBS, fileHash);
        Utils.writeContents(blob, (Object) content);
    }

    /**
     * Create a directory for filesystem
     */
    private static void mkdir(File f) {
        if (!f.mkdir()) {
            Abort("Fail to construct gitlet filesystem");
        }
    }

    /**
     * Return the file names inside a folder in the gitlet repo
     */
    private static List<String> listFiles(File folder) {
        List<String> files = Utils.plainFilenamesIn(folder);
        if (files == null) {
            Abort("File system is broken, init a new gitlet repo!");
        }
        return files;
    }
}
