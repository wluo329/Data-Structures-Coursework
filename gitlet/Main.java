package gitlet;

import java.io.File;
import java.io.Serializable;
import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author William Luo
 */
public class Main implements Serializable {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        File file = join(".gitlet", "gitlet");
        Repository git;
        if (file.exists()) {
            git = readObject(file, Repository.class);
        } else {
            git = new Repository();
            if (!args[0].equals("init")) {
                System.out.println("Not in an initialized Gitlet directory.");
                System.exit(0);
            }
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                if (args.length == 1) {
                    git.initRepo();
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "add":
                if (args.length == 2) {
                    git.add(args[1]);
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "commit":
                if (args.length == 2) {
                    git.commit(args[1]);
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "rm":
                if (args.length == 2) {
                    git.rm(args[1]);
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "log":
                if (args.length == 1) {
                    git.log();
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "global-log":
                if (args.length == 1) {
                    git.globalLog();
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "find":
                if (args.length == 2) {
                    git.find(args[1]);
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "status":
                if (args.length == 1) {
                    git.status();
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "checkout":
                git.checkout(args);
                break;
            case "branch":
                if (args.length == 2) {
                    git.branch(args[1]);
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "rm-branch":
                if (args.length == 2) {
                    git.rmBranch(args[1]);
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "reset":
                if (args.length == 2) {
                    git.reset(args[1]);
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "merge":
                if (args.length == 2) {
                    git.merge(args[1]);
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
        writeObject(file, git);
    }
}

