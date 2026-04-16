package gitlet;

import java.io.File;
import java.util.List;
import static gitlet.Main.Abort;
import static gitlet.Utils.sha1;

// TODO: three improvement -> use hash or content as the contract for this class, not the file name,
// TODO: delete the 1 time usage helper method
// TODO: all methods have pre-condition, valid parameters
/** Represents gitlet repository filesystem and provide IO operations
 * .gitlet/ filesystem
 * .gitlet/ -- gitlet repository
 *    - objects/ -- folder containing blobs and commits
 *       - commits/ -- folder containing serialized commits named by its hash
 *       - blobs/ -- folder containing blobs named by its hash
 *    - refs/ -- folder tracking the local and remote branch header
 *       - heads/ -- folder tracking local branch header
 *          - master -- file containing a string of branch hash
 *          - xx -- other branches
 *    - HEAD -- file containing the branch name under refs/heads/ head pointer points to
 *    - index -- file(staging area) tracking files for addition or removal
 */
public class GitletIO {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    private static final File GITLET = Utils.join(CWD, ".gitlet");
    /** Initial directories of gitlet filesystem */
    private static final File OBJECTS = Utils.join(GITLET, "objects");
    private static final File REFS = Utils.join(GITLET, "refs");
    private static final File COMMITS = Utils.join(OBJECTS, "commits");
    private static final File BLOBS = Utils.join(OBJECTS, "blobs");
    private static final File HEADS = Utils.join(REFS, "heads");
    /** Initial files of gitlet filesystem */
    private static final File HEAD = Utils.join(GITLET, "HEAD");
    private static final File INDEX = Utils.join(GITLET, "index");

    /** Initial branch name */
    private static final String defaultBranch = "master";

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
        setHead(defaultBranch);
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
        File fp = parsePath(hash);
        if (fp == null) {
            Abort("No commit with that id exists.");
        }
        return Utils.readObject(fp, Commit.class);
    }

    /**
     * Return the file pointer if input represents a commit, null otherwise
     * @param hash valid format: 4-40 characters long, each represent a
     *             lower case hex number, without any prefix like 0x, 0X, etc.
     * With valid format, it should also indicate a unique commit without ambiguity
     */
    private static File parsePath(String hash) {
        if (!hash.matches("^[0-9a-f]{4,40}$")) {
            return null;
        }
        if (hash.length() == 40) {
            File fp = Utils.join(COMMITS, hash);
            return fp.exists() ? fp : null;
        }
        int num = 0;
        String fullHash = null;
        // TODO: may change this for better performance
        for (String commitHash : listFiles(COMMITS)) {
            if (commitHash.startsWith(hash)) {
                num++;
                fullHash = commitHash;
            }
        }
        if (num == 1) {
            return Utils.join(COMMITS, fullHash);
        }
        return null;
    }

    /**
     * Save the serialized object content to filesystem
     * Only need the content, use hash as the name is just the design choice
     */
    public static void saveCommit(byte[] content) {
        String hash = sha1((Object) content);
        File fp = Utils.join(COMMITS, hash);
        Utils.writeContents(fp, (Object) content);
    }

    /**
     * Returns the hash of all commits in the repo
     */
    public static List<String> getCommits() {
        return listFiles(COMMITS);
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
     * Update the branch pointer to a commit
     * Create new branch if not exists
     */
    public static void updateBranch(String branchName, String commitHash) {
        File fp = Utils.join(HEADS, branchName);
        Utils.writeContents(fp, commitHash);
    }

    /**
     * Check if the name represents a valid branch, in O(1) time
     */
    public static boolean isBranch(String branchName) {
        return Utils.join(HEADS, branchName).exists();
    }

    /**
     * Return branch hash
     */
    public static String getBranch(String branchName) {
        File fp = Utils.join(HEADS, branchName);
        return Utils.readContentsAsString(fp);
    }

    public static void rmBranch(String branchName) {
        File fp = Utils.join(HEADS, branchName);
        Utils.restrictedDelete(fp);
    }

    /**
     * Return all the branch names of this repo
     */
    public static List<String> getBranches() {
        return listFiles(HEADS);
    }

    /**
     * Check if a branch has been initialized
     */
    public static boolean existBranch() {
        File fp = Utils.join(HEADS, defaultBranch);
        return fp.exists();
    }

    /**
     * Set head pointer to the branch
     */
    public static void setHead(String branchName) {
        Utils.writeContents(HEAD, branchName);
    }

    /**
     * Return the hash of the head of current branch
     */
    public static String headHash() {
        return getBranch(head());
    }

    /**
     * Return the branch name head pointer point to
     */
    public static String head() {
        return Utils.readContentsAsString(HEAD);
    }

    /* IO for blobs and working directory files */
    /**
     * Check whether the file is in the CWD
     */
    public static boolean inCWD(String fileName) {
        File fp = Utils.join(CWD, fileName);
        return fp.exists();
    }

    /**
     * remove file from the CWD
     */
    public static void rmCWD(String fileName) {
        File fp = Utils.join(CWD, fileName);
        Utils.restrictedDelete(fp);
    }

    /**
     * create or overwrite file in the CWD with a file tracked by the commit
     */
    public static void writeCWD(String fileName, String blobHash) {
        File bp = Utils.join(BLOBS, blobHash);
        File fp = Utils.join(CWD, fileName);
        Utils.writeContents(fp, (Object) Utils.readContents(bp));
    }

    /**
     * get files in the CWD
     */
    public static List<String> getCWD() {
        return listFiles(CWD);
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
     * Return the file names inside a folder in the gitlet repo
     */
    private static List<String> listFiles(File dir) {
        List<String> files = Utils.plainFilenamesIn(dir);
        if (files == null) {
            Abort("File system is broken, init a new gitlet repo!");
        }
        return files;
    }

    /**
     * Create a directory for filesystem
     */
    private static void mkdir(File dir) {
        if (!dir.mkdir()) {
            Abort("Fail to construct gitlet filesystem");
        }
    }

}
