package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     * TODO: when init, init the Master and HEAD, and change it when commit
     * TODO: Only add the changed file into staging area
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The objects, refs directory and HEAD, index file. */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEAD_FI = join(GITLET_DIR, "HEAD");
    public static final File INDEX_FI = join(GITLET_DIR, "index");
    /** The refs/heads subdirectory. */
    public static final File HEADS_DIR = join(REFS_DIR, "heads");

    /**
     * init .gitlet/ filesystem
     * .gitlet/ -- gitlet repository
     *    - objects/ -- folder containing blobs and commits
     *       - xx/ -- folder named by the first 2 number of an object hash
     *          - xx -- file named by the rest 38 number,
     *               -- containing persistent data for commit, byte data for blob
     *    - refs/ -- folder tracking the local and remote branch header
     *       - heads/ -- folder tracking local branch header
     *          - master -- file containing a string of branch hash
     *          - xx -- other branches
     *    - HEAD -- file containing a string of commit hash the HEAD point to
     *    - index -- file(staging area) containing serialized (fileName, blobHash) pair
     */
    public static void initFilesystem() {
        //TODO: handle commit later
        if (GITLET_DIR.exists()) {
            Utils.message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        try {
            if (!GITLET_DIR.mkdir() || !OBJECTS_DIR.mkdir() || !REFS_DIR.mkdir() ||
                !HEAD_FI.createNewFile() || !INDEX_FI.createNewFile() ||
                !HEADS_DIR.mkdir()) {
                throw new IOException("Fail to build filesystem");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* TODO: fill in the rest of this class. */
}
