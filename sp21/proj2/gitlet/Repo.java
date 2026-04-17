package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.GitletIO.CWD;
import static gitlet.Main.Abort;
import static gitlet.Utils.sha1;

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
        if (sameAs(curr, f)) {
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
            boolean sameAs = inCWD && sameAs(commit, f);
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
        GitletIO.isInRepo();
        replaceCWD(commitId);
        GitletIO.updateBranch(GitletIO.head(), commitId);
        Index index = Index.fromFile();
        index.abandon();
        index.saveIndex();
    }

    public static void merge(String branchName) {
        GitletIO.isInRepo();
        if (!GitletIO.isBranch(branchName)) {
            Abort("A branch with that name does not exist.");
        } else if (GitletIO.head().equals(branchName)) {
            Abort("Cannot merge a branch with itself.");
        }
        Commit curr = Commit.fromFile(GitletIO.headHash());
        Commit mergedIn = Commit.fromFile(GitletIO.getBranch(branchName));
        Commit ancestor = latestAncestor(curr, mergedIn);
        /* Easy cases */
        if (ancestor.equals(mergedIn)) {
            Abort("Given branch is an ancestor of the current branch.");
        } else if (ancestor.equals(curr)) {
            reset(GitletIO.getBranch(branchName));
            Abort("Current branch fast-forwarded.");
        }
        /* Complex case, compare fileHash between thw three */
        Set<String> fileNames = new HashSet<>();
        fileNames.addAll(curr.trackedFiles());
        fileNames.addAll(mergedIn.trackedFiles());
        fileNames.addAll(ancestor.trackedFiles());

        for (String f : fileNames) {
            /* Keep current work: only current branch modify/delete the file */
        }
    }

    /**
     * Check whether file contents in two commits are the same or untracked.
     */
    private static boolean sameBlob(Commit one, Commit other, String fileName) {
        return one.fileHash(fileName).equals(other.fileHash(fileName));
    }

    /**
     * Helper class for searching latest ancestor
     */
    private static class Node {
        /** Wrap a commit object */
        public final Commit self;
        /** Track the offspring(head of branch) */
        public final Commit offspring;

        public Node(Commit self, Commit offspring) {
            this.self = self;
            this.offspring = offspring;
        }

        /**
         * Check whether the input is its offspring
         */
        public boolean isOffspring(Commit commit) {
            return commit.equals(offspring);
        }

        /**
         * Return the parents wrapper of this commit
         */
        public List<Node> getParents() {
            List<Node> parents = new ArrayList<>();
            for (String parentHash : self.getParents()) {
                Commit parent = Commit.fromFile(parentHash);
                parents.add(new Node(parent, offspring));
            }
            return parents;
        }

        public Date timeStamp() {
            return self.getDate();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Node)) {
                return false;
            }
            Node other = (Node) obj;
            return self.equals(other.self);
        }
    }

    /**
     * Find the latest ancestor for two branch heads in a commit DAG
     * Take in commit hash
     */
    private static Commit latestAncestor(Commit one, Commit other) {
        Set<Node> oneFamily = new HashSet<>();
        Set<Node> otherFamily = new HashSet<>();
        // Contains operation is slow in PQ, avoid add duplicate items to PQ
        // Construct max heap because Date.compareTo() returns positive for newer date
        Queue<Node> pq = new PriorityQueue<>(
                Comparator.comparing(Node::timeStamp, Comparator.reverseOrder())
        );
        Node oneNode = new Node(one, one);
        Node otherNode = new Node(other, other);
        // Add the two the set, fix the fast-forward case
        oneFamily.add(oneNode);
        otherFamily.add(otherNode);
        pq.add(oneNode);
        pq.add(otherNode);

        while (true) {
            Node latest = pq.remove();
            // Will triggered by initial commit anyway
            if (oneFamily.contains(latest) && otherFamily.contains(latest)) {
                return latest.self;
            }
            List<Node> parents = latest.getParents();
            if (latest.isOffspring(one)) {
                oneFamily.addAll(parents);
            } else {
                otherFamily.addAll(parents);
            }
            for (Node parent : parents) {
                if (!oneFamily.contains(parent) && !otherFamily.contains(parent)) {
                    pq.add(parent);
                }
            }
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
}
