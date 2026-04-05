package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;

// TODO: try modify the objects/ to mimic git later
/** Represents a gitlet repository.
 * .gitlet/ filesystem
 * .gitlet/ -- gitlet repository
 *    - objects/ -- folder containing blobs and commits
 *       - commits/ -- folder containing serialized commits named by its hash
 *       - blobs/ -- folder containing blobs named by its hash
 *    - refs/ -- folder tracking the local and remote branch header
 *       - heads/ -- folder tracking local branch header
 *          - master -- file containing a string of branch hash
 *          - xx -- other branches
 *    - HEAD -- file containing a string indicating which head under refs/heads it points to
 *    - index -- file(staging area) containing serialized (fileName, blobHash) pair
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The objects, refs directory and HEAD, index file. */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    // TODO: add will change objects/ and INDEX_FI
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
            Utils.message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        if (!GITLET_DIR.mkdir() || !OBJECTS_DIR.mkdir() || !COMMITS_DIR.mkdir() || !BLOBS_DIR.mkdir() ||
            !REFS_DIR.mkdir() || !HEADS_DIR.mkdir()) {
            Utils.message("Fail to construct gitlet filesystem");
            System.exit(0);
        }
        // Default HEAD
        Utils.writeContents(Repository.HEAD_FI, "ref: refs/heads/master");
        makeCommit("initial commit");
    }

    /**
     * Create a commit object and save it to gitlet filesystem
     */
    private static void makeCommit(String message) {
        Commit commit = new Commit(message);
        commit.saveCommit();
    }
}
