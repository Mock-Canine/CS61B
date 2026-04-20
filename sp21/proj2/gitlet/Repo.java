package gitlet;

import java.io.File;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

import static gitlet.Main.abort;
import static gitlet.Utils.sha1;

public class Repo {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** Initial branch name */
    public static final String DEFAULT_BRANCH = "master";
    /** Filesystem represent repo under CWD */
    public static final FileSystem REPO = new FileSystem(CWD);

    /**
     * Init gitlet repo and make initial commit
     */
    public static void init() {
        REPO.init();
        REPO.setHead(DEFAULT_BRANCH);
        String hash = initialCommit();
        REPO.updateBranch(REPO.head(), hash);
    }

    /**
     * Add a file to staging area
     */
    public static void add(String fileName) {
        if (!inCWD(fileName)) {
            abort("File does not exist.");
        }
        Commit curr = Commit.fromFile(REPO.headHash());
        Index index = Index.fromFile();
        index.unstageForRemoval(fileName);
        if (sameAs(curr, fileName)) {
            index.unstageForAddition(fileName);
        } else {
            index.stageForAddition(fileName);
        }
        index.save();
    }

    /**
     * Make a commit
     */
    public static void commit(String message) {
        String hash = makeCommit(message, "");
        REPO.updateBranch(REPO.head(), hash);
    }

    /**
     * Remove a file from staging area
     */
    public static void rm(String fileName) {
        Commit curr = Commit.fromFile(REPO.headHash());
        Index index = Index.fromFile();
        boolean isTracked = curr.isTracked(fileName);
        boolean isStaged = index.isStaged(fileName);
        if (!isTracked && !isStaged) {
            abort("No reason to remove the file.");
        }
        index.unstageForAddition(fileName);
        if (isTracked) {
            rmCWD(fileName);
            index.stageForRemoval(fileName);
        }
        index.save();
    }

    public static void log() {
        Commit.printHistory(REPO.headHash());
    }

    public static void globalLog() {
        for (String hash : REPO.getCommits()) {
            Commit commit = Commit.fromFile(hash);
            System.out.print(commit);
        }
    }

    public static void find(String message) {
        boolean isFound = false;
        for (String hash : REPO.getCommits()) {
            Commit commit = Commit.fromFile(hash);
            if (commit.getMessage().equals(message)) {
                isFound = true;
                System.out.println(hash);
            }
        }
        if (!isFound) {
            abort("Found no commit with that message.");
        }
    }

    public static void status() {
        printBranches();
        printStagingArea();
        printNotStaged();
        printUntracked();
    }

    public static void checkout(String[] args) {
        Map<String, String> params = parseCheckout(args);
        String branchName = params.get("branchName");
        if (branchName == null) {
            String commitHash = params.get("commitHash");
            if (commitHash == null) {
                commitHash = REPO.headHash();
            }
            checkoutFile(commitHash, params.get("fileName"));
        } else {
            checkoutBranch(branchName);
        }
    }

    public static void branch(String branchName) {
        if (REPO.isBranch(branchName)) {
            abort("A branch with that name already exists.");
        }
        REPO.updateBranch(branchName, REPO.headHash());
    }

    public static void rmBranch(String branchName) {
        if (!REPO.isBranch(branchName)) {
            abort("A branch with that name does not exist.");
        }
        if (REPO.head().equals(branchName)) {
            abort("Cannot remove the current branch.");
        }
        REPO.rmBranch(branchName);
    }

    public static void reset(String commitHash) {
        replaceCWD(commitHash);
        REPO.updateBranch(REPO.head(), commitHash);
        Index.resetIndex();
    }

    public static void merge(String branchName) {
        /* Handle exceptions */
        if (!REPO.isBranch(branchName)) {
            abort("A branch with that name does not exist.");
        } else if (REPO.head().equals(branchName)) {
            abort("Cannot merge a branch with itself.");
        }
        Index index = Index.fromFile();
        if (!index.isEmpty()) {
            abort("You have uncommitted changes.");
        }

        /* Easy cases */
        Commit curr = Commit.fromFile(REPO.headHash());
        String branchHash = REPO.getBranch(branchName);
        Commit mergedIn = Commit.fromFile(branchHash);
        Commit ancestor = latestAncestor(curr, mergedIn);
        untrackedAbort(curr, mergedIn);
        if (ancestor.equals(mergedIn)) {
            abort("Given branch is an ancestor of the current branch.");
        } else if (ancestor.equals(curr)) {
            reset(branchHash);
            abort("Current branch fast-forwarded.");
        }

        /* Complex case, compare fileHash between the three */
        Set<String> fileNames = new HashSet<>();
        fileNames.addAll(curr.getFiles());
        fileNames.addAll(mergedIn.getFiles());
        fileNames.addAll(ancestor.getFiles());
        boolean hasConflict = false;
        for (String f : fileNames) {
            /* Indicate the pair with same file state */
            boolean sameCurrMerge = sameBlob(curr, mergedIn, f);
            boolean sameCurrAnc = sameBlob(curr, ancestor, f);
            boolean sameMergeAnc = sameBlob(mergedIn, ancestor, f);
            /* Keep current work:
             * Only current branch create/modify/delete the file
             * No change for both of them
             * */
            if (sameMergeAnc) {
                continue;
            }
            /* Take in other branch's work:
             * Only other branch create/modify/delete the file
             * */
            if (sameCurrAnc) {
                if (mergedIn.isTracked(f)) {
                    writeCWD(f, REPO.getBlob(mergedIn.getBlobHash(f)));
                    index.stageForAddition(f);
                } else {
                    rmCWD(f);
                    index.stageForRemoval(f);
                }
            /* Both change the work */
            } else {
                /* Same way change -> nothing to do */
                /* Different way change */
                if (!sameCurrMerge) {
                    conflictFile(curr, mergedIn, f);
                    index.stageForAddition(f);
                    hasConflict = true;
                }

            }
        }
        // Necessary, because manipulating staging area + make commit will happen in one command,
        // but makeCommit() will retrieve index from file
        index.save();
        String hash = makeCommit("Merged " + branchName + " into " + REPO.head() + ".", branchHash);
        REPO.updateBranch(REPO.head(), hash);
        if (hasConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    public static void addRemote(String[] args) {
        // TODO: change the way to manipulate config later, repo does not need to know the config
        String name = args[1];
        String path = args[2];
        Config config = Config.fromFile();
        if (config.isRemote(name)) {
            abort("A remote with that name already exists.");
        }
        // TODO: method should not rely on the existence of remoteName folder
        REPO.initRemote(name);
        config.addRemote(name, Paths.get(path).toFile());
    }

    public static void rmRemote(String name) {
        Config config = Config.fromFile();
        if (!config.isRemote(name)) {
            abort("A remote with that name does not exist.");
        }
        config.rmRemote(name);
    }

    public static void fetch(String[] args) {
        String name = args[1];
        String branchName = args[2];
        Config config = Config.fromFile();
        // Assume config has recorded the remote repo
        File path = config.getPath(name);
        // TODO: may be a bug
        if (!path.exists()) {
            abort("Remote directory not found.");
        }
        FileSystem remoteRepo = new FileSystem(path.getParentFile());
        if (!remoteRepo.isBranch(branchName)) {
            abort("That remote does not have that branch.");
        }
        String hash = remoteRepo.getBranch(branchName);
        REPO.updateRemoteBranch(name, branchName, hash);
        saveRemoteCommits(remoteRepo, hash);
    }

    /**
     * Retrieve non-common commits and blobs from a remote repo branch and save to local repo
     */
    private static void saveRemoteCommits(FileSystem remoteRepo, String hash) {
        Queue<String> queue = new ArrayDeque<>();
        Set<String> marked = new HashSet<>();
        queue.add(hash);
        while (!queue.isEmpty()) {
            hash = queue.poll();
            marked.add(hash);
            Commit commit = Commit.fromFile(hash, remoteRepo);
            // If it is tracked, its parents must have been tracked
            if (REPO.isCommit(hash)) {
                continue;
            }
            saveRemoteCommit(remoteRepo, commit);
            for (String parentHash : commit.getParents()) {
                if (!marked.contains(parentHash)) {
                    queue.add(parentHash);
                }
            }
        }
    }

    private static void saveRemoteCommit(FileSystem remoteRepo, Commit commit) {
        byte[] content = Utils.serialize(commit);
        REPO.saveCommit(commit.getHash(), content);
        for (String blobHash : commit.getBlobs().values()) {
            if (!REPO.isBlob(blobHash)) {
                REPO.saveBlob(blobHash, remoteRepo.getBlob(blobHash));
            }
        }
    }

    /**
     * Modify the conflict file content for merge
     */
    private static void conflictFile(Commit curr, Commit other, String fileName) {
        String content = "";
        if (curr.isTracked(fileName)) {
            // Rely on the newline of the file itself
            content += """
                <<<<<<< HEAD
                %s\
                """.formatted((Object) REPO.getBlob(curr.getBlobHash(fileName)));
        } else {
            content += """
                <<<<<<< HEAD
                """;
        }
        if (other.isTracked(fileName)) {
            content += """
                =======
                %s\
                >>>>>>>
                """.formatted((Object) REPO.getBlob(other.getBlobHash(fileName)));
        } else {
            content += """
                =======
                >>>>>>>
                """;
        }
        File fp = Utils.join(CWD, fileName);
        Utils.writeContents(fp, content);
    }

    /**
     * Check whether file contents in two commits are the same or untracked.
     */
    private static boolean sameBlob(Commit one, Commit other, String fileName) {
        return one.getBlobHash(fileName).equals(other.getBlobHash(fileName));
    }

    /**
     * Find the latest ancestor for two commits in a commit DAG
     */
    private static Commit latestAncestor(Commit one, Commit other) {
        TreeSet<Commit> ancestorsOfOne = findAncestors(one);
        TreeSet<Commit> ancestorsOfOther = findAncestors(other);
        TreeSet<Commit> commonAncestors = new TreeSet<>(ancestorsOfOne);
        commonAncestors.retainAll(ancestorsOfOther);
        return commonAncestors.first();
    }

    /**
     * Find all the ancestors(include itself) for a commit
     * Return the tree set of its ancestors
     */
    private static TreeSet<Commit> findAncestors(Commit commit) {
        // Smallest item in the set has the newest date
        TreeSet<Commit> ancestors = new TreeSet<>(
                Comparator.comparing(Commit::getDate, Comparator.reverseOrder())
        );
        Queue<Commit> queue = new ArrayDeque<>();
        queue.add(commit);
        while (!queue.isEmpty()) {
            commit = queue.poll();
            ancestors.add(commit);
            for (String parentHash : commit.getParents()) {
                Commit parent = Commit.fromFile(parentHash);
                if (!ancestors.contains(parent)) {
                    queue.add(parent);
                }
            }
        }
        return ancestors;
    }

    /**
     * Replace files in CWD with files tracked by a commit
     */
    private static void replaceCWD(String commitHash) {
        Commit curr = Commit.fromFile(REPO.headHash());
        Commit checkout = Commit.fromFile(commitHash);
        untrackedAbort(curr, checkout);
        // Only delete tracked files, not all workdir files(some files not tracked by both commits)
        for (String fileName : curr.getFiles()) {
            rmCWD(fileName);
        }
        for (String fileName : checkout.getFiles()) {
            writeCWD(fileName, REPO.getBlob(checkout.getBlobHash(fileName)));
        }
    }

    /**
     * Helper method to check untracked files when merging or checkout
     */
    private static void untrackedAbort(Commit curr, Commit other) {
        List<String> workFiles = getCWD();
        for (String fileName : workFiles) {
            if (!curr.isTracked(fileName) && other.isTracked(fileName)) {
                abort("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }
    }

    /**
     * Print view of branches, mark current branch with *
     */
    private static void printBranches() {
        List<String> branches = REPO.getBranches();
        branches.sort(null);
        String head = REPO.head();
        System.out.println("=== Branches ===");
        for (String b : branches) {
            if (b.equals(head)) {
                System.out.println("*" + b);
            } else {
                System.out.println(b);
            }
        }
        System.out.println();
    }

    private static void printStagingArea() {
        Index index = Index.fromFile();
        System.out.print(index);
    }

    private static void printNotStaged() {
        System.out.print("""
                === Modifications Not Staged For Commit ===
                
                """);
    }

    private static void printUntracked() {
        System.out.print("""
                === Untracked Files ===
                
                """);
    }

    /**
     * Create and save initial commit to filesystem
     * Return the newly made commit hash
     */
    private static String initialCommit() {
        Commit commit = new Commit("initial commit", Date.from(Instant.EPOCH));
        return commit.getHash();
    }

    /**
     * Create and save a commit to filesystem
     * Provide mergedIn branch hash if you do a merge commit, empty string otherwise
     * Return the newly made commit hash
     */
    private static String makeCommit(String message, String mergedIn) {
        if (message.isEmpty()) {
            abort("Please enter a commit message.");
        }
        Commit parent = Commit.fromFile(REPO.headHash());
        // Cp content, not reference, this can not be detected by test
        // because the change to parent's blobs will not be saved to file
        Map<String, String> blobs = new HashMap<>(parent.getBlobs());
        Index index = Index.fromFile();
        if (index.isEmpty()) {
            abort("No changes added to the commit.");
        }
        index.commit(blobs);
        index.save();
        Commit commit;
        if (!mergedIn.isEmpty()) {
            commit = new Commit(message, REPO.headHash(), mergedIn, blobs, Date.from(Instant.now()));
        } else {
            commit = new Commit(message, REPO.headHash(), blobs, Date.from(Instant.now()));
        }
        return commit.getHash();
    }

    /**
     * Check whether the file in the CWD is the same as in the commit
     * Assume file exists
     */
    private static boolean sameAs(Commit commit, String fileName) {
        File fp = Utils.join(CWD, fileName);
        byte[] content = Utils.readContents(fp);
        String fileHash = sha1((Object) content);
        String blobHash = commit.getBlobHash(fileName);
        return fileHash.equals(blobHash);
    }

    /**
     * Parse the arguments for checkout command,
     * returns a map which may contains keys [branchName, commitHash, fileName],
     */
    private static Map<String, String> parseCheckout(String[] args) {
        int len = args.length;
        Map<String, String> map = new HashMap<>();
        if (len == 3 && args[1].equals("--")) {
            map.put("fileName", args[2]);
        } else if (len == 4 && args[2].equals("--")) {
            map.put("commitHash", args[1]);
            map.put("fileName", args[3]);
        } else if (len == 2) {
            map.put("branchName", args[1]);
        } else {
            abort("Incorrect operands.");
        }
        return map;
    }

    private static void checkoutFile(String commitHash, String fileName) {
        Commit commit = Commit.fromFile(commitHash);
        String blobHash = commit.getBlobHash(fileName);
        if (!commit.isTracked(fileName)) {
            abort("File does not exist in that commit.");
        }
        writeCWD(fileName, REPO.getBlob(blobHash));
    }

    private static void checkoutBranch(String branchName) {
        if (!REPO.isBranch(branchName)) {
            abort("No such branch exists.");
        } else if (branchName.equals(REPO.head())) {
            abort("No need to checkout the current branch.");
        } else {
            replaceCWD(REPO.getBranch(branchName));
            REPO.setHead(branchName);
            Index.resetIndex();
        }
    }

    /**
     * Check whether the file is in the CWD
     */
    private static boolean inCWD(String fileName) {
        File fp = Utils.join(CWD, fileName);
        return fp.exists();
    }

    /**
     * remove file from the CWD
     */
    private static void rmCWD(String fileName) {
        File fp = Utils.join(CWD, fileName);
        Utils.restrictedDelete(fp);
    }

    /**
     * create or overwrite file in the CWD
     */
    private static void writeCWD(String fileName, byte[] content) {
        File fp = Utils.join(CWD, fileName);
        Utils.writeContents(fp, (Object) content);
    }

    /**
     * get files in the CWD
     */
    private static List<String> getCWD() {
        List<String> files = Utils.plainFilenamesIn(CWD);
        assert files != null;
        return files;
    }
}
