package gitlet;

import java.util.HashMap;
import java.util.Map;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author MockCanine
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Abort("Please enter a command.");
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                validateNumArgs(args, 1);
                Repo.init();
                break;
            case "add":
                validateNumArgs(args, 2);
                GitletIO.isInRepo();
                Repo.add(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2);
                GitletIO.isInRepo();
                Repo.commit(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2);
                GitletIO.isInRepo();
                Repo.rm(args[1]);
                break;
            case "log":
                validateNumArgs(args, 1);
                GitletIO.isInRepo();
                Repo.log();
                break;
            case "global-log":
                validateNumArgs(args, 1);
                GitletIO.isInRepo();
                Repo.globalLog();
                break;
            case "find":
                validateNumArgs(args, 2);
                GitletIO.isInRepo();
                Repo.find(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1);
                GitletIO.isInRepo();
                Repo.status();
                break;
            case "checkout":
                GitletIO.isInRepo();
                Repo.checkout(parseCheckout(args));
                break;
            case "branch":
                validateNumArgs(args, 2);
                GitletIO.isInRepo();
                Repo.branch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                GitletIO.isInRepo();
                Repo.rmBranch(args[1]);
                break;
            case "reset":
                validateNumArgs(args, 2);
                GitletIO.isInRepo();
                Repo.reset(args[1]);
                break;
            case "merge":
                validateNumArgs(args, 2);
                GitletIO.isInRepo();
                Repo.merge(args[1]);
                break;
            default:
                Abort("No command with that name exists.");
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * Print message and exit program if not match
     */
    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            Abort("Incorrect operands.");
        }
    }

    /**
     * Parse the arguments for checkout command,
     * returns a map which may contains keys [branchName, commitId, fileName],
     */
    public static Map<String, String> parseCheckout(String[] args) {
        int len = args.length;
        Map<String, String> map = new HashMap<>();
        if (len == 3 && args[1].equals("--")) {
            map.put("fileName", args[2]);
        } else if (len == 4 && args[2].equals("--")) {
            map.put("commitId", args[1]);
            map.put("fileName", args[3]);
        } else if (len == 2) {
            map.put("branchName", args[1]);
        } else {
            Abort("Incorrect operands.");
        }
        return map;
    }

    /**
     * Print message and abort program
     */
    public static void Abort(String msg) {
        Utils.message(msg);
        System.exit(0);
    }
}
