package gitlet;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

import static gitlet.Main.abort;
import static gitlet.Utils.sha1;

public class Repo {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** Initial branch name */
    public static final String DEFAULT_BRANCH = "master";
    /** Filesystem represent repo under CWD */
    public static final fileSystem repo = new fileSystem(CWD);

    /**
     * Init gitlet repo and make initial commit
     */
    public static void init() {
        repo.init();
        repo.setHead(DEFAULT_BRANCH);
        String hash = initialCommit();
        repo.updateBranch(repo.head(), hash);
    }

    /**
     * Add a file to staging area
     */
    public static void add(String fileName) {
        if (!repo.inCWD(fileName)) {
            abort("File does not exist.");
        }
        Commit curr = Commit.fromFile(repo.headHash());
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
        repo.updateBranch(repo.head(), hash);
    }

    /**
     * Remove a file from staging area
     */
    public static void rm(String fileName) {
        Commit curr = Commit.fromFile(repo.headHash());
        Index index = Index.fromFile();
        boolean isTracked = curr.isTracked(fileName);
        boolean isStaged = index.isStaged(fileName);
        if (!isTracked && !isStaged) {
            abort("No reason to remove the file.");
        }
        index.unstageForAddition(fileName);
        if (isTracked) {
            repo.rmCWD(fileName);
            index.stageForRemoval(fileName);
        }
        index.save();
    }

    public static void log() {
        Commit.printHistory(repo.headHash());
    }

    public static void globalLog() {
        for (String hash : repo.getCommits()) {
            Commit commit = Commit.fromFile(hash);
            System.out.print(commit);
        }
    }

    public static void find(String message) {
        boolean isFound = false;
        for (String hash : repo.getCommits()) {
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
                commitHash = repo.headHash();
            }
            checkoutFile(commitHash, params.get("fileName"));
        } else {
            checkoutBranch(branchName);
        }
    }

    public static void branch(String branchName) {
        if (repo.isBranch(branchName)) {
            abort("A branch with that name already exists.");
        }
        repo.updateBranch(branchName, repo.headHash());
    }

    public static void rmBranch(String branchName) {
        if (!repo.isBranch(branchName)) {
            abort("A branch with that name does not exist.");
        }
        if (repo.head().equals(branchName)) {
            abort("Cannot remove the current branch.");
        }
        repo.rmBranch(branchName);
    }

    public static void reset(String commitHash) {
        replaceCWD(commitHash);
        repo.updateBranch(repo.head(), commitHash);
        Index.resetIndex();
    }

    public static void merge(String branchName) {
        /* Handle exceptions */
        if (!repo.isBranch(branchName)) {
            abort("A branch with that name does not exist.");
        } else if (repo.head().equals(branchName)) {
            abort("Cannot merge a branch with itself.");
        }
        Index index = Index.fromFile();
        if (!index.isEmpty()) {
            abort("You have uncommitted changes.");
        }

        /* Easy cases */
        Commit curr = Commit.fromFile(repo.headHash());
        String branchHash = repo.getBranch(branchName);
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
        fileNames.addAll(curr.trackedFiles());
        fileNames.addAll(mergedIn.trackedFiles());
        fileNames.addAll(ancestor.trackedFiles());
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
                    repo.writeCWD(f, mergedIn.fileHash(f));
                    index.stageForAddition(f);
                } else {
                    repo.rmCWD(f);
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
        String hash = makeCommit("Merged " + branchName + " into " + repo.head() + ".", branchHash);
        repo.updateBranch(repo.head(), hash);
        if (hasConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    public static void addRemote(String[] args) {
        // TODO: change the way to manipulate config later
        String name = args[1];
        String path = args[2];
        Config config = Config.fromFile();
        if (config.isRemote(name)) {
            abort("A remote with that name already exists.");
        }
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
        if (!config.isRemote(name)) {
            abort("A remote with that name does not exist.");
        }
        File path = config.getPath(name);
        if (!path.exists()) {
            abort("Remote directory not found.");
        }
//        repo.fetchRemote(path, branchName);
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
                """.formatted(repo.getBlob(curr.fileHash(fileName)));
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
                """.formatted(repo.getBlob(other.fileHash(fileName)));
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
        return one.fileHash(fileName).equals(other.fileHash(fileName));
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
    private static TreeSet<Commit> findAncestors(Commit target) {
        // Smallest item in the set has the newest date
        TreeSet<Commit> ancestors = new TreeSet<>(
                Comparator.comparing(Commit::getDate, Comparator.reverseOrder())
        );
        Queue<Commit> queue = new ArrayDeque<>();
        queue.add(target);
        while (!queue.isEmpty()) {
            Commit commit = queue.poll();
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
        Commit curr = Commit.fromFile(repo.headHash());
        Commit checkout = Commit.fromFile(commitHash);
        untrackedAbort(curr, checkout);
        // Only delete tracked files, not all workdir files(some files not tracked by both commits)
        for (String fileName : curr.trackedFiles()) {
            repo.rmCWD(fileName);
        }
        for (String fileName : checkout.trackedFiles()) {
            repo.writeCWD(fileName, checkout.fileHash(fileName));
        }
    }

    /**
     * Helper method to check untracked files when merging or checkout
     */
    private static void untrackedAbort(Commit curr, Commit other) {
        List<String> workFiles = repo.getRoot();
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
        List<String> branches = repo.getBranches();
        branches.sort(null);
        String head = repo.head();
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
        Commit commit = new Commit("initial commit");
        commit.save();
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
        Commit parent = Commit.fromFile(repo.headHash());
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
            commit = new Commit(message, repo.headHash(), mergedIn, blobs);
        } else {
            commit = new Commit(message, repo.headHash(), blobs);
        }
        commit.save();
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
        String blobHash = commit.fileHash(fileName);
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
        String blobHash = commit.fileHash(fileName);
        if (!commit.isTracked(fileName)) {
            abort("File does not exist in that commit.");
        }
        repo.writeCWD(fileName, blobHash);
    }

    private static void checkoutBranch(String branchName) {
        if (!repo.isBranch(branchName)) {
            abort("No such branch exists.");
        } else if (branchName.equals(repo.head())) {
            abort("No need to checkout the current branch.");
        } else {
            replaceCWD(repo.getBranch(branchName));
            repo.setHead(branchName);
            Index.resetIndex();
        }
    }
}
