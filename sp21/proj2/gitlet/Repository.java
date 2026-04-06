package gitlet;

import java.io.File;

import static gitlet.Utils.*;

// TODO: try modify the objects/ to mimic git later
/** Represents a gitlet repository and manipulates file operations
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
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The objects, refs directory and HEAD, index file. */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEAD_FI = join(GITLET_DIR, "HEAD");
    public static final File INDEX_FI = join(GITLET_DIR, "index");
    /** The subdirectories of objects/ and refs/. */
    public static final File COMMITS_DIR = join(OBJECTS_DIR, "commits");
    public static final File BLOBS_DIR = join(OBJECTS_DIR, "blobs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");

    /**
     * Init .gitlet filesystem and make initial commit
     */
    public static void init() {
        if (GITLET_DIR.exists()) {
            message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        // Create whole filesystem
        if (!GITLET_DIR.mkdir() || !OBJECTS_DIR.mkdir() || !COMMITS_DIR.mkdir() || !BLOBS_DIR.mkdir() ||
            !REFS_DIR.mkdir() || !HEADS_DIR.mkdir()) {
            message("Fail to construct gitlet filesystem");
            System.exit(0);
        }
        // Default HEAD, point to branch master
        writeContents(Repository.HEAD_FI, "master");
        // Build empty staging area
        Index index = new Index();
        index.saveIndex();

        makeCommit("initial commit");
    }

    public static void add(String f) {
        isInRepo();
        File file = join(CWD, f);
        if (!file.exists()) {
            message("File does not exist.");
            System.exit(0);
        }
        Commit curr = Commit.fromFile("HEAD");
        Index index = Index.fromFile();

        byte[] content = readContents(file);
        String fileHash = sha1(content);
        String commitHash = curr.blobs.get(f);
        // do not use state machine, use rule-based method
        // And the ADT often support both create or overwrite operation in one function(like remove)
        index.indexRm.remove(f);
        if (fileHash.equals(commitHash)) {
            index.indexAdd.remove(f);
        } else {
            index.indexAdd.put(f, fileHash);
            File blob = join(BLOBS_DIR, fileHash);
            writeContents(blob, content);
        }
        index.saveIndex();
    }

    public static void commit(String message) {
        isInRepo();
        if (message.isEmpty()) {
            message("Please enter a commit message.");
            System.exit(0);
        }
        makeCommit(message);
    }

    public static void rm(String f) {
        isInRepo();
        Commit curr = Commit.fromFile("HEAD");
        Index index = Index.fromFile();
        boolean inCommit = curr.blobs.containsKey(f);
        boolean inIndex = index.indexAdd.containsKey(f);
        if (!inCommit && !inIndex) {
            message("No reason to remove the file.");
            System.exit(0);
        }
        index.indexAdd.remove(f);
        if (inCommit) {
            // Remove from work dir
            restrictedDelete(f);
            index.indexRm.add(f);
        }
        // TODO: saveIndex every time is like bad feeling
        index.saveIndex();
    }

    /**
     * Create a commit object and save it to gitlet filesystem
     */
    // TODO: is it useful to maintain a commit tree and serialize when needed?
    private static void makeCommit(String message) {
        Commit commit = new Commit(message);
        commit.saveCommit();
    }

    private static void isInRepo() {
        if (!GITLET_DIR.exists()) {
            message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}
