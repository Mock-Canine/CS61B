package gitlet;

import java.io.File;

// TODO: try modify the objects/ to mimic git later
/** Represents gitlet repository filesystem
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
public class FileSystem {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET = Utils.join(CWD, ".gitlet");
    /** Initial directories of gitlet filesystem */
    public static final File OBJECTS = Utils.join(GITLET, "objects");
    public static final File REFS = Utils.join(GITLET, "refs");
    public static final File COMMITS = Utils.join(OBJECTS, "commits");
    public static final File BLOBS = Utils.join(OBJECTS, "blobs");
    public static final File HEADS = Utils.join(REFS, "heads");
    /** Initial files of gitlet filesystem */
    public static final File HEAD = Utils.join(GITLET, "HEAD");
    public static final File INDEX = Utils.join(GITLET, "index");
}
