package gitlet;

import java.io.File;
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
            // Remove from work dir
            restrictedDelete(f);
            index.stageForRemoval(f);
        }
        // TODO: saveIndex every time is like bad feeling
        index.saveIndex();
    }

    public static void log() {
        // TODO: add merge support later
        GitletIO.isInRepo();
        Commit.printHistory(Commit.headHash());
    }

    public static void globalLog() {
        GitletIO.isInRepo();
        for (String name : plainFilenamesIn(COMMITS_DIR)) {
            Commit commit = Commit.fromFile(name);
            System.out.println(commit);
        }
    }

    public static void find(String message){
        GitletIO.isInRepo();
        boolean isFound = false;
        for (String name : plainFilenamesIn(COMMITS_DIR)) {
            Commit commit = Commit.fromFile(name);
            if (commit.getMessage().equals(message)) {
                isFound = true;
                System.out.println(commit.getHash());
            }
        }
        if (!isFound) {
            message("Found no commit with that message.");
            System.exit(0);
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
        String branch = args.get("branchName");
        if (branch == null) {
            String hash = args.get("commitId");
            if (hash == null) {
                hash = GitletIO.headHash();
            }
            String name = args.get("fileName");
            Commit commit = Commit.fromFile(hash);
            String blobHash = commit.blobHash(name);
            if (blobHash == null) {
                Abort("File does not exist in that commit.");
            }
            File blob = join(BLOBS_DIR, blobHash);
            File file = join(CWD, name);
            writeContents(file, (Object) readContents(blob));
        } else {
            if (!plainFilenamesIn(HEADS_DIR).contains(branch)) {
                Abort("No such branch exists.");
            } else if (branch.equals(Commit.head())) {
                Abort("No need to checkout the current branch.");
            } else {
                File checkoutBranch = join(HEADS_DIR, branch);
                String checkoutHash = readContentsAsString(checkoutBranch);
                Commit curr = Commit.fromFile(Commit.headHash());
                Commit checkout = Commit.fromFile(checkoutHash);
                List<String> workFiles = plainFilenamesIn(CWD);
                for (String f : workFiles) {
                    if (!curr.tracked(f) && checkout.tracked(f)) {
                        Abort("There is an untracked file in the way; delete it, or add and commit it first.");
                    }
                }
                for (String f : workFiles) {
                    restrictedDelete(f);
                }
                for (String name : checkout.trackedFiles()) {
                    File file = join(BLOBS_DIR, checkout.blobHash(name));
                    File workFile = join(CWD, name);
                    writeContents(workFile, (Object) readContents(file));
                }
                // Change the HEAD
                writeContents(HEAD_FI, branch);
                // TODO: may be just overwrite empty thing into the index?
                Index index = Index.fromFile();
                index.clear();
                index.saveIndex();
            }
        }
    }

    public static void branch(String name) {
        GitletIO.isInRepo();
        for (String b : plainFilenamesIn(HEADS_DIR)) {
            if (b.equals(name)) {
                Abort("A branch with that name already exists.");
            }
        }
        File newBranch = join(HEADS_DIR, name);
        writeContents(newBranch, Commit.headHash());
    }

    /**
     * Create a commit object and save it to gitlet filesystem
     */
    private static void makeCommit(String message) {
        Commit commit = new Commit(message);
        commit.save();
    }
}
