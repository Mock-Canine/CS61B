package gitlet;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static gitlet.Main.abort;
import static gitlet.Utils.sha1;

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
 *       - remotes/ -- folder for remote repos
 *          - xx/ -- folder named by remote repo
 *             - xx -- file containing a string of remote branch hash
 *    - HEAD -- file containing the branch name under refs/heads/ head pointer points to
 *    - config -- file containing location of remote repos
 *    - index -- file(staging area) tracking files for addition or removal
 */
public class fileSystem {
    /** The root directory of this gitlet filesystem */
    private final File root;
    /** The .gitlet directory. */
    private final File gitlet;
    /** Initial directories of gitlet filesystem */
    private final File objects;
    private final File refs;
    private final File commits;
    private final File blobs;
    private final File heads;
    /** Initial files of gitlet filesystem */
    private final File head;
    private final File index;
    /** directory for remote repos */
    private final File remotes;
    /** file for remote repos */
    private final File config;

    public fileSystem(File root) {
        this.root = root;
        gitlet = Utils.join(root, ".gitlet");
        objects = Utils.join(gitlet, "objects");
        refs = Utils.join(gitlet, "refs");
        commits = Utils.join(objects, "commits");
        blobs = Utils.join(objects, "blobs");
        heads = Utils.join(refs, "heads");
        head = Utils.join(gitlet, "HEAD");
        index = Utils.join(gitlet, "index");
        remotes = Utils.join(refs, "remotes");
        config = Utils.join(gitlet, "config");
    }

    /**
     * Init the gitlet filesystem
     */
    public void init() {
        if (gitlet.exists()) {
            abort("A Gitlet version-control system already exists in the current directory.");
        }
        // Create required folders, order of creating matters
        mkdir(gitlet);
        mkdir(objects);
        mkdir(refs);
        mkdir(commits);
        mkdir(blobs);
        mkdir(heads);
        // Create clean staging area and config
        Index.resetIndex();
        Config.initConfig();
    }

    /**
     * Check if in a gitlet repo
     */
    public void isInRepo() {
        if (!gitlet.exists()) {
            abort("Not in an initialized Gitlet directory.");
        }
    }

    /* IO for commit operations, call the corresponding version in Commit to get and save */
    /**
     * Retrieve a commit object from file
     * @param commitHash valid format: 4-40 characters long, each represent a
     *             lower case hex number, without any prefix like 0x, 0X, etc.
     * With valid format, it should also indicate a unique commit without ambiguity
     * Abort the program if provide invalid hash
     */
    public Commit getCommit(String commitHash) {
        File fp = parsePath(commitHash);
        if (fp == null) {
            abort("No commit with that id exists.");
        }
        return Utils.readObject(fp, Commit.class);
    }

    /**
     * Return the file pointer if input represents a commit, null otherwise
     * @param hash valid format: 4-40 characters long, each represent a
     *             lower case hex number, without any prefix like 0x, 0X, etc.
     * With valid format, it should also indicate a unique commit without ambiguity
     */
    private File parsePath(String hash) {
        if (!hash.matches("^[0-9a-f]{4,40}$")) {
            return null;
        }
        int hashLen = 40;
        if (hash.length() == hashLen) {
            File fp = Utils.join(commits, hash);
            return fp.exists() ? fp : null;
        }
        int num = 0;
        String fullHash = null;
        for (String commitHash : listFiles(commits)) {
            if (commitHash.startsWith(hash)) {
                num++;
                fullHash = commitHash;
            }
        }
        if (num == 1) {
            return Utils.join(commits, fullHash);
        }
        return null;
    }

    /**
     * Save the serialized commit object to filesystem
     */
    public void saveCommit(byte[] content) {
        // Only need the content as param, use hash as the name is just the design choice
        String hash = sha1((Object) content);
        File fp = Utils.join(commits, hash);
        Utils.writeContents(fp, (Object) content);
    }

    /**
     * Returns the hash of all commits in the repo
     */
    public List<String> getCommits() {
        return listFiles(commits);
    }

    /* IO for index operations, call the corresponding version in Index to get and save */
    public Index getIndex() {
        return Utils.readObject(index, Index.class);
    }

    public void saveIndex(Index index) {
        Utils.writeObject(this.index, index);
    }

    /* IO for branch operations */
    /**
     * Update the branch pointer to a commit.
     * Create new branch if not exists.
     * Assume valid commitHash.
     */
    public void updateBranch(String branchName, String commitHash) {
        File fp = Utils.join(heads, branchName);
        Utils.writeContents(fp, commitHash);
    }

    /**
     * Check if the name represents a valid branch
     */
    public boolean isBranch(String branchName) {
        // Do not use listFiles(), this is in O(1) time
        return Utils.join(heads, branchName).exists();
    }

    /**
     * Return branch hash
     * Assume branchName is valid
     */
    public String getBranch(String branchName) {
        File fp = Utils.join(heads, branchName);
        return Utils.readContentsAsString(fp);
    }

    /**
     * Remove branch
     * Assume branchName is valid
     */
    public void rmBranch(String branchName) {
        File fp = Utils.join(heads, branchName);
        if (!fp.delete()) {
            abort("Fail to remove a branch.");
        }
    }

    /**
     * Return all the branch names of this repo
     */
    public List<String> getBranches() {
        return listFiles(heads);
    }

    /**
     * Set head pointer to the branch
     * Assume branchName is valid
     */
    public void setHead(String branchName) {
        Utils.writeContents(head, branchName);
    }

    /**
     * Return the hash of the head of current branch
     */
    public String headHash() {
        return getBranch(head());
    }

    /**
     * Return the branch name head pointer point to
     */
    public String head() {
        return Utils.readContentsAsString(head);
    }

    /* IO for blobs and working directory files */
    /**
     * Check whether the file is in the CWD
     */
    public boolean inCWD(String fileName) {
        File fp = Utils.join(root, fileName);
        return fp.exists();
    }

    /**
     * remove file from the CWD
     */
    public void rmCWD(String fileName) {
        File fp = Utils.join(root, fileName);
        Utils.restrictedDelete(fp);
    }

    /**
     * create or overwrite file in the CWD with a file tracked by a commit
     */
    public void writeCWD(String fileName, String blobHash) {
        File bp = Utils.join(blobs, blobHash);
        File fp = Utils.join(root, fileName);
        Utils.writeContents(fp, (Object) Utils.readContents(bp));
    }

    /**
     * get files in the CWD
     */
    public List<String> getRoot() {
        return listFiles(root);
    }

    /**
     * Save the file in CWD to blobs
     */
    public void saveBlob(String fileHash, byte[] content) {
        File blob = Utils.join(blobs, fileHash);
        Utils.writeContents(blob, (Object) content);
    }

    /**
     * Return the content of a blob by its hash
     */
    public String getBlob(String blobHash) {
        File fp = Utils.join(blobs, blobHash);
        return Utils.readContentsAsString(fp);
    }

    /* IO for remote operations */
    /**
     * Retrieve config from its file
     */
    public Config getConfig() {
        return Utils.readObject(config, Config.class);
    }

    /**
     * Save config to its file
     */
    public void saveConfig(Config config) {
        Utils.writeObject(this.config, config);
    }

    /**
     * Remove the actual folder for remote if exists
     * Assume valid remote name
     */
    public void rmRemoteDir(String name) {
        File fp = Utils.join(remotes, name);
        if (fp.exists()) {
            if (!fp.delete()) {
                abort("Fail to remove the remote directory");
            }
        }
    }

    /**
     * Fetch from a remote repo
     * Assume valid path, remote name
     */
    public void fetchRemote(File path, String name, String branchName) {
        File remoteBranch = Utils.join(path, relativePath(heads), branchName);
        if (!remoteBranch.exists()) {
            abort("That remote does not have that branch.");
        }
        mkRemoteDir(name);
        String headHash = Utils.readContentsAsString(remoteBranch);
        File branch = Utils.join(remotes, name, branchName);
        Utils.writeContents(branch, headHash);
    }

    private void mkRemoteDir(String name) {
        if (!remotes.exists()) {
            mkdir(remotes);
        }
        File remote = Utils.join(remotes, name);
        if (!remote.exists()) {
            mkdir(remote);
        }
    }

    /**
     * Retrieve non-common commits and blobs from remote repo and save to local repo
     */
    private void saveRemoteCommits(File path, String headHash) {
        File remoteCommits = Utils.join(path, relativePath(commits));
        File remoteBlobs = Utils.join(path, relativePath(blobs));
        Queue<String> pq = new ArrayDeque<>();
        Set<String> marked = new HashSet<>();
        pq.add(headHash);
        while (!pq.isEmpty()) {
            String hash = pq.poll();
        }
    }

    /**
     * Return a string view of relative path between repo root and any sub folder/file
     */
    private String relativePath(File subFolder) {
        Path root = gitlet.toPath();
        Path sub = subFolder.toPath();
        return root.relativize(sub).toString();
    }

    /**
     * Return the file names inside a folder in the gitlet repo
     */
    private List<String> listFiles(File dir) {
        List<String> files = Utils.plainFilenamesIn(dir);
        if (files == null) {
            abort("File system is broken, init a new gitlet repo!");
        }
        return files;
    }

    /**
     * Create a directory for filesystem
     */
    private void mkdir(File dir) {
        if (!dir.mkdir()) {
            abort("Fail to construct gitlet filesystem");
        }
    }

}
