package gitlet;

import static gitlet.Utils.*;
/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author MockCanine
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            message("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(args, 1);
                Repository.init();
                break;
            case "add":
                validateNumArgs(args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2);
                Repository.rm(args[1]);
                break;
            case "log":
                validateNumArgs(args, 1);
                Repository.log();
                break;
            default:
                message("No command with that name exists.");
                System.exit(0);
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * Print message and exit program if not match
     */
    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            message("Incorrect operands.");
            System.exit(0);
        }
    }
}
