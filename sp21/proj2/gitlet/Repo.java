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

    public static void add(String f) {
        isInRepo();
        File file = join(CWD, f);
        if (!file.exists()) {
            message("File does not exist.");
            System.exit(0);
        }
        Commit curr = Commit.fromFile(Commit.headHash());
        Index index = Index.fromFile();
        byte[] content = readContents(file);
        String fileHash = sha1((Object) content);
        // File may not be tracked by the commit
        String blobHash = curr.blobHash(f);
        // do not use state machine, use rule-based method
        // And the ADT often support both create or overwrite operation in one function(like remove)
        index.unstageForRemoval(f);
        if (fileHash.equals(blobHash)) {
            index.unstageForAddition(f);
        } else {
            index.stageForAddition(f, fileHash);
            File blob = join(BLOBS_DIR, fileHash);
            writeContents(blob, (Object) content);
        }
        index.saveIndex();
    }

    public static void commit(String message) {
        isInRepo();
        makeCommit(message);
    }

    public static void rm(String f) {
        isInRepo();
        Commit curr = Commit.fromFile(Commit.headHash());
        Index index = Index.fromFile();
        boolean inCommit = curr.inCommit(f);
        boolean stageForAddition = index.isStaged(f);
        if (!inCommit && !stageForAddition) {
            message("No reason to remove the file.");
            System.exit(0);
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
        isInRepo();
        Commit.printHistory(Commit.headHash());
    }

    public static void globalLog() {
        isInRepo();
        for (String name : plainFilenamesIn(COMMITS_DIR)) {
            Commit commit = Commit.fromFile(name);
            System.out.println(commit);
        }
    }

    public static void find(String message){
        isInRepo();
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
        isInRepo();
        String branch = args.get("branchName");
        if (branch == null) {
            String hash = args.get("commitId");
            if (hash == null) {
                hash = Commit.headHash();
            }
            String name = args.get("fileName");
            Commit commit = Commit.fromFile(hash);
            String blobHash = commit.blobHash(name);
            if (blobHash == null) {
                message("File does not exist in that commit.");
                System.exit(0);
            }
            File blob = join(BLOBS_DIR, blobHash);
            File file = join(CWD, name);
            writeContents(file, (Object) readContents(blob));
        } else {
            if (!plainFilenamesIn(HEADS_DIR).contains(branch)) {
                message("No such branch exists.");
                System.exit(0);
            } else if (branch.equals(Commit.head())) {
                message("No need to checkout the current branch.");
                System.exit(0);
            } else {
                File checkoutBranch = join(HEADS_DIR, branch);
                String checkoutHash = readContentsAsString(checkoutBranch);
                Commit curr = Commit.fromFile(Commit.headHash());
                Commit checkout = Commit.fromFile(checkoutHash);
                List<String> workFiles = plainFilenamesIn(CWD);
                for (String f : workFiles) {
                    if (!curr.inCommit(f) && checkout.inCommit(f)) {
                        message("There is an untracked file in the way; delete it, or add and commit it first.");
                        System.exit(0);
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
        isInRepo();
        for (String b : plainFilenamesIn(HEADS_DIR)) {
            if (b.equals(name)) {
                message("A branch with that name already exists.");
                System.exit(0);
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

    private static void isInRepo() {
        if (!GITLET_DIR.exists()) {
            message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}
