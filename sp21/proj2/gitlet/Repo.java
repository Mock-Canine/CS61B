package gitlet;

import java.util.*;

import static gitlet.Main.Abort;

public class Repo {
    /**
     * Init gitlet repo and make initial commit
     */
    public static void init() {
        GitletIO.init();
        makeCommit("initial commit");
    }

    /**
     * Manipulate staging area for a file
     */
    public static void add(String f) {
        GitletIO.isInRepo();
        if (!GitletIO.inCWD(f)) {
            Abort("File does not exist.");
        }
        Commit curr = Commit.fromFile(GitletIO.headHash());
        Index index = Index.fromFile();
        index.unstageForRemoval(f);
        if (curr.sameAs(f)) {
            index.unstageForAddition(f);
        } else {
            index.stageForAddition(f);
        }
        index.saveIndex();
    }

    public static void commit(String message) {
        GitletIO.isInRepo();
        makeCommit(message);
    }

    public static void rm(String f) {
        GitletIO.isInRepo();
        Commit curr = Commit.fromFile(GitletIO.headHash());
        Index index = Index.fromFile();
        boolean inCommit = curr.tracked(f);
        boolean stageForAddition = index.isStaged(f);
        if (!inCommit && !stageForAddition) {
            Abort("No reason to remove the file.");
        }
        index.unstageForAddition(f);
        if (inCommit) {
            GitletIO.rmCWD(f);
            index.stageForRemoval(f);
        }
        index.saveIndex();
    }

    public static void log() {
        GitletIO.isInRepo();
        Commit.printHistory(GitletIO.headHash());
    }

    public static void globalLog() {
        GitletIO.isInRepo();
        for (String hash : GitletIO.getCommits()) {
            Commit commit = Commit.fromFile(hash);
            System.out.println(commit);
        }
    }

    public static void find(String message){
        GitletIO.isInRepo();
        boolean isFound = false;
        for (String hash : GitletIO.getCommits()) {
            Commit commit = Commit.fromFile(hash);
            if (commit.getMessage().equals(message)) {
                isFound = true;
                System.out.println(hash);
            }
        }
        if (!isFound) {
            Abort("Found no commit with that message.");
        }
    }

    public static void status() {
        GitletIO.isInRepo();
        Commit commit = Commit.fromFile(GitletIO.headHash());
        Index index = Index.fromFile();
        Set<String> files = index.addition();
        files.addAll(commit.trackedFiles());
        files.addAll(GitletIO.getCWD());

        List<String> notStaged = new ArrayList<>();
        List<String> untracked = new ArrayList<>();
        for (String f : files) {
            boolean staged = index.isStaged(f);
            boolean tracked = commit.tracked(f);
            boolean inCWD = GitletIO.inCWD(f);
            boolean stagedRm = index.isStagedRm(f);
            if (inCWD && !staged && (!tracked || stagedRm)) {
                untracked.add(f);
                continue;
            }
            boolean sameAs = inCWD && commit.sameAs(f);
            boolean sameInIndex = inCWD && index.sameAs(f);

            boolean caseA = tracked && !sameAs && staged;
            boolean caseB = staged && !sameInIndex;

            boolean caseC = staged && !inCWD;
            boolean caseD = !stagedRm && tracked && !inCWD;
            if (caseA || caseB) {
                notStaged.add(f + " (modified)");
            } else if (caseC || caseD) {
                notStaged.add(f + " (deleted)");
            }
        }
        notStaged.sort(null);
        untracked.sort(null);

        printBranches();
        printStagingArea();
        printNotStaged(notStaged);
        printUntracked(untracked);
    }

    /**
     * Handle three usages of checkout, input params must be valid for usages
     * takes a map which may contains keys [branchName, commitId, fileName],
     */
    public static void checkout(Map<String, String> args) {
        GitletIO.isInRepo();
        String branchName = args.get("branchName");
        if (branchName == null) {
            String commitId = args.get("commitId");
            if (commitId == null) {
                commitId = GitletIO.headHash();
            }
            String f = args.get("fileName");
            Commit commit = Commit.fromFile(commitId);
            String blobHash = commit.fileHash(f);
            if (!commit.tracked(f)) {
                Abort("File does not exist in that commit.");
            }
            GitletIO.writeCWD(f, blobHash);
        } else {
            if (!GitletIO.isBranch(branchName)) {
                Abort("No such branch exists.");
            } else if (branchName.equals(GitletIO.head())) {
                Abort("No need to checkout the current branch.");
            } else {
                replaceCWD(GitletIO.getBranch(branchName));
                GitletIO.setHead(branchName);
                Index index = Index.fromFile();
                index.abandon();
                index.saveIndex();
            }
        }
    }

    public static void branch(String name) {
        GitletIO.isInRepo();
        if (GitletIO.isBranch(name)) {
            Abort("A branch with that name already exists.");
        }
        GitletIO.updateBranch(name, GitletIO.headHash());
    }

    public static void rmBranch(String name) {
        GitletIO.isInRepo();
        if (!GitletIO.isBranch(name)) {
            Abort("A branch with that name does not exist.");
        }
        if (GitletIO.head().equals(name)) {
            Abort("Cannot remove the current branch.");
        }
        GitletIO.rmBranch(name);
    }

    public static void reset(String commitId) {
        replaceCWD(commitId);
        GitletIO.updateBranch(GitletIO.head(), commitId);
        Index index = Index.fromFile();
        index.abandon();
        index.saveIndex();
    }

    public static void merge(String branchName) {
    }

    /**
     * Find the latest ancestor for two branch heads in a commit DAG
     */
    private static Commit latestAncestor(Commit one, Commit theOther) {
        Set<Commit> oneAncestor = new HashSet<>();
        Set<Commit> theOtherAncestor = new HashSet<>();
        PriorityQueue<Commit> pq = new PriorityQueue<>();
        pq.add(one);
        pq.add(theOther);
        while (!pq.isEmpty()) {
            Commit node = pq.remove();

        }
    }

    /**
     * Replace files in CWD with files tracked by a commit
     */
    private static void replaceCWD(String commitId) {
        Commit curr = Commit.fromFile(GitletIO.headHash());
        Commit checkout = Commit.fromFile(commitId);
        List<String> workFiles = GitletIO.getCWD();
        for (String fileName : workFiles) {
            if (!curr.tracked(fileName) && checkout.tracked(fileName)) {
                Abort("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }
        // Only delete tracked files, not all workdir files(some files not tracked by both commits)
        for (String fileName : curr.trackedFiles()) {
            GitletIO.rmCWD(fileName);
        }
        for (String fileName : checkout.trackedFiles()) {
            GitletIO.writeCWD(fileName, checkout.fileHash(fileName));
        }
    }

    /**
     * Print view of branches, mark current branch with *
     */
    private static void printBranches() {
        List<String> branches = GitletIO.getBranches();
        branches.sort(null);
        String head = GitletIO.head();
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
        System.out.println(index);
    }

    private static void printNotStaged(List<String> src) {
        System.out.printf("""
                === Modifications Not Staged For Commit ===
                %s
                %n""", String.join("\n", src));
    }

    private static void printUntracked(List<String> src) {
        System.out.printf("""
                === Untracked Files ===
                %s
                """, String.join("\n", src));
    }

    /**
     * Create and save a commit automatically
     */
    private static void makeCommit(String message) {
        new Commit(message);
    }

    private static void mergeCommit(String message, String mergedIn) {
        new Commit(message, mergedIn);
    }
}
