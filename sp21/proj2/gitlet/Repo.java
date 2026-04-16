package gitlet;

import java.util.List;
import java.util.Map;

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
        // TODO: saveIndex every time is like bad feeling
        index.saveIndex();
    }

    public static void log() {
        // TODO: add merge support later
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
     * Create a commit object and save it to gitlet filesystem
     */
    private static void makeCommit(String message) {
        Commit commit = new Commit(message);
        commit.save();
    }
}
