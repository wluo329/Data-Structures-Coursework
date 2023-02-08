package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author William Luo
 */
public class Repository implements Serializable {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    private String cwd;
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    private ArrayList<String> allCommits;
    private Storage stagingArea;
    private String currentHead;
    private String currentBranch;
    private ArrayList<String> branches;
    private HashMap<String, String> branchHeads;
    private HashSet<String> tracked;
    private ArrayList<String> untracked;

    private String ancestorHolder;
    private int min;

    public Repository() {
        cwd = System.getProperty("user.dir");
        allCommits = new ArrayList<>();
        stagingArea = new Storage();
        branches = new ArrayList<>();
        branchHeads = new HashMap<>();
        tracked = new HashSet<>();
        untracked = new ArrayList<>();
        min = Integer.MAX_VALUE;
    }

    /** Handles the init command */
    public void initRepo() {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
            Commit initialCommit = new Commit();
            String initialCommitName = initialCommit.getHashID();
            allCommits.add(initialCommitName);
            File test = join(GITLET_DIR, initialCommitName);
            writeObject(test, initialCommit);
            currentHead = initialCommitName;
            currentBranch = "master";
            branches.add(currentBranch);
            branchHeads.put("master", initialCommitName);
            ancestorHolder = currentHead;
        } else {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
            System.exit(0);
        }
    }

    public void add(String fileName) {
        Blob b;
        File addedFile = new File(fileName);
        if (addedFile.exists()) {
            b = new Blob(fileName);
            String sha1Hash = b.getBlobHash();
            File file = join(GITLET_DIR, currentHead);
            Commit prevCommit = readObject(file, Commit.class);
            if (prevCommit.getBlobs().contains(sha1Hash)) {
                stagingArea.remove(fileName);
            } else {
                stagingArea.insert(fileName);
            }
            tracked.add(fileName);
            if (untracked.contains(fileName)) {
                untracked.remove(fileName);
            }
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
        }
    }

    public void commit(String msg) {
        if (!msg.isEmpty()) {
            File file = join(GITLET_DIR, currentHead);
            Commit prevCommit = readObject(file, Commit.class);
            HashMap<String, String> snapshot = new HashMap<>();
            HashMap<String, String> prevFiles = prevCommit.getContents();
            for (String filename: tracked) {
                if (stagingArea.getContents().containsKey(filename)) {
                    snapshot.put(filename, stagingArea.getContents().get(filename));
                } else if (!untracked.contains(filename)) {
                    if (prevFiles.containsKey(filename)) {
                        snapshot.put(filename, prevFiles.get(filename));
                    }
                }
            }
            if (prevFiles.equals(snapshot)) {
                System.out.println("No changes added to the commit.");
                System.exit(0);
            } else {
                stagingArea = new Storage();
                Commit newCommit = new Commit(msg, snapshot, currentHead, currentBranch);
                String name = newCommit.getHashID();
                allCommits.add(name);
                currentHead = name;
                branchHeads.put(currentBranch, name);
                File file1 = join(GITLET_DIR, name);
                writeObject(file1, newCommit);
                untracked.clear();
            }
        } else {
            System.out.print("Please enter a commit message.");
            System.exit(0);
        }

    }

    public void rm(String fileName) {
        File file = join(GITLET_DIR, currentHead);
        Commit headCommit = readObject(file, Commit.class);
        HashMap<String, String> headFiles = headCommit.getContents();
        if (stagingArea.contains(fileName) || headFiles.containsKey(fileName)) {
            if (stagingArea.contains(fileName)) {
                stagingArea.remove(fileName);
            }
            if (headFiles.containsKey(fileName)) {
                untracked.add(fileName);
                File copy = new File(fileName);
                restrictedDelete(copy);
            }
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    public void log() {
        File file = join(GITLET_DIR, currentHead);
        Commit currCommit = readObject(file, Commit.class);
        while (currCommit.getParent() != null) {
            System.out.println("===");
            System.out.println("commit " + currCommit.getHashID());
            if (currCommit.getMergeParent() != null) {
                String parID1 = currCommit.getParent().substring(0, 7);
                String parID2 = currCommit.getMergeParent().substring(0, 7);
                System.out.println("Merge: " + parID1 + " " + parID2);
            }
            System.out.println("Date: " + currCommit.getTime());
            System.out.println(currCommit.getMessage());
            System.out.println("");
            file = join(GITLET_DIR, currCommit.getParent());
            currCommit = readObject(file, Commit.class);
        }
        System.out.println("===");
        System.out.println("commit " + currCommit.getHashID());
        System.out.println("Date: " + currCommit.getTime());
        System.out.println(currCommit.getMessage());
        System.out.println();
    }

    public void globalLog() {
        for (String commit: allCommits) {
            System.out.println("===");
            File copy = join(GITLET_DIR, commit);
            Commit currCommit = readObject(copy, Commit.class);
            System.out.println("commit " + currCommit.getHashID());
            if (currCommit.getMergeParent() != null) {
                String parID1 = currCommit.getParent().substring(0, 7);
                String parID2 = currCommit.getMergeParent().substring(0, 7);
                System.out.println("Merge: " + parID1 + " " + parID2);
            }
            System.out.println("Date: " + currCommit.getTime());
            System.out.println(currCommit.getMessage());
            System.out.println("");
        }
    }

    public void find(String msg) {
        int commitsFound = 0;
        for (String commit: allCommits) {
            File copy = join(GITLET_DIR, commit);
            Commit currCommit = readObject(copy, Commit.class);
            if (msg.equals(currCommit.getMessage())) {
                System.out.println(currCommit.getHashID());
                commitsFound++;
            }
        }
        if (commitsFound == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void status() {
        SortedMap<String, String> heads = new TreeMap<>(branchHeads);
        System.out.println("=== Branches ===");
        for (String branch: heads.keySet()) {
            if (!branch.equals(currentBranch)) {
                System.out.println(branch);
            } else {
                System.out.println("*" + branch);
            }
        }
        System.out.println("");

        System.out.println("=== Staged Files ===");
        for (String branch: stagingArea.getContents().keySet()) {
            System.out.println(branch);
        }
        System.out.println("");

        System.out.println("=== Removed Files ===");
        for (String branch: untracked) {
            System.out.println(branch);
        }
        System.out.println("");
        //EC
        List<String> wd = plainFilenamesIn(cwd);
        Commit currCommit = readObject(join(GITLET_DIR, currentHead), Commit.class);
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String s : currCommit.getContents().keySet()) {
            if (wd.contains(s)) {
                Blob b = new Blob(s);
                if (!b.getBlobHash().equals(currCommit.getContents().get(s))
                        && !stagingArea.contains(s)) {
                    System.out.println(s + " (modified)");
                }
            } else {
                if (!untracked.contains(s)) {
                    System.out.println(s + " (deleted)");
                }
            }
        }
        for (String s : stagingArea.getContents().keySet()) {
            if (wd.contains(s)) {
                Blob b = new Blob(s);
                if (!b.getBlobHash().equals(
                        stagingArea.getContents().get(s))) {
                    System.out.println(s + " (modified)");
                }
            } else {
                System.out.println(s + " (deleted)");
            }
        }
        System.out.println("");
        System.out.println("=== Untracked Files ===");
        for (String s : wd) {
            if (!tracked.contains(s)) {
                if (!stagingArea.contains(s)) {
                    System.out.println(s);
                }
            }
        }
        System.out.println("");
    }

    public void checkout(String[] args) {
        if (args.length == 3) { //filecheckout
            if (args[1].equals("--")) {
                fileCheckout(args[2]);
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        } else if (args.length == 4) { //fileCheckout with id
            if (args[2].equals("--")) {
                fileIDCheckout(args[3], args[1]);
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        } else if (args.length == 2) { //branchCheckout
            branchCheckout(args[1]);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    private void fileCheckout(String fileName) {
        fileIDCheckout(fileName, currentHead);
    }

    private void fileIDCheckout(String fileName, String commitHash) {
        if (commitHash.length() >= currentHead.length()) {
            File file = join(GITLET_DIR, commitHash);
            if (!file.exists()) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
            Commit prevCommit = readObject(file, Commit.class);
            String prevHash = prevCommit.getContents().get(fileName);
            if (prevCommit.getContents().containsKey(fileName)) {
                file = join(GITLET_DIR, prevHash);
                Blob b = readObject(file, Blob.class);
                file = new File(fileName);
                writeContents(file, b.getContents());
            } else {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }
        } else {
            int commitsFound = 0;
            for (String commit : allCommits) {
                if (commitHash.equals(commit.substring(0, commitHash.length()))) {
                    fileIDCheckout(fileName, commit);
                    commitsFound++;
                }
            }
            if (commitsFound == 0) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
        }
    }

    private void branchCheckout(String branchName) {
        if (!branchHeads.keySet().contains(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (branchName.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        File copy = join(GITLET_DIR, branchHeads.get(branchName));
        Commit branch = readObject(copy, Commit.class);
        HashMap<String, String> branchFiles = branch.getContents();
        List<String> wdList = plainFilenamesIn(cwd);
        for (String fn : wdList) {
            if (!tracked.contains(fn)) {
                if (branchFiles.keySet().contains(fn)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        for (String tFile : tracked) {
            if (!branchFiles.containsKey(tFile)) {
                restrictedDelete(tFile);
            }
        }
        tracked.clear();
        for (String b : branchFiles.keySet()) {
            String hash = branchFiles.get(b);
            Blob c = readObject(join(GITLET_DIR, hash), Blob.class);
            writeContents(new File(b), c.getContents());
            tracked.add(b);
        }
        currentBranch = branchName;
        currentHead = branchHeads.get(branchName);
        stagingArea.clear();
    }

    public void branch(String addedBranch) {
        if (!branches.contains(addedBranch)) {
            branches.add(addedBranch);
            branchHeads.put(addedBranch, currentHead);
        } else {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
    }

    public void rmBranch(String removedBranch) {
        if (!branches.contains(removedBranch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (removedBranch.equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        } else {
            branches.remove(removedBranch);
            branchHeads.remove(removedBranch);
        }
    }

    public void reset(String commit) {
        if (!allCommits.contains(commit)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        File file = join(GITLET_DIR, commit);
        Commit copy = readObject(file, Commit.class);
        HashMap<String, String> files = copy.getContents();
        List<String> wdList = plainFilenamesIn(cwd);
        for (String fn : wdList) {
            if (!tracked.contains(fn)) {
                if (files.keySet().contains(fn)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        for (String fn : wdList) {
            if (tracked.contains(fn)) {
                if (!files.keySet().contains(fn)) {
                    File deletedFile = new File(fn);
                    deletedFile.delete();
                }
            }
        }
        tracked.clear();
        for (String tFile : files.keySet()) {
            tracked.add(tFile);
            fileIDCheckout(tFile, commit);
        }
        stagingArea.clear();
        currentHead = commit;
        branchHeads.put(currentBranch, commit);
    }

    public void merge(String branchName) {
        String givenHash = branchHeads.get(branchName);
        String currentHash = branchHeads.get(currentBranch);
        if (!stagingArea.getContents().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (givenHash == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (givenHash.equals(currentHash)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        String splitPt = findAncestor(givenHash, currentHash, branchName, currentBranch);
        Commit given = readObject(join(GITLET_DIR, givenHash), Commit.class);
        Commit current = readObject(join(GITLET_DIR, currentHash), Commit.class);
        Commit splitPoint = readObject(join(GITLET_DIR, splitPt), Commit.class);
        HashMap<String, String> branchFiles = given.getContents();
        List<String> wdList = plainFilenamesIn(cwd);
        for (String fn : wdList) {
            if (!tracked.contains(fn)) {
                if (branchFiles.keySet().contains(fn)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        if (splitPoint.getHashID().equals(givenHash)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitPoint.getHashID().equals(currentHash)) {
            branchCheckout(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        min = Integer.MAX_VALUE;
        ancestorHolder = currentHead;
        HashSet<String> allFiles = new HashSet<>();
        for (String spFile : splitPoint.getContents().keySet()) {
            allFiles.add(spFile); }
        for (String gFile : given.getContents().keySet()) {
            allFiles.add(gFile); }
        for (String cFile : current.getContents().keySet()) {
            allFiles.add(cFile); }
        HashMap<String, String> givenContents = given.getContents();
        HashMap<String, String> currentContents = current.getContents();
        HashMap<String, String> splitPointContents = splitPoint.getContents();
        boolean conflict = mergeHelper(allFiles, givenContents, currentContents,
                splitPointContents, branchName, given);
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
        HashMap<String, String> stagingFiles = stagingArea.getContents();
        HashMap<String, String> allNewFiles = new HashMap<>();
        for (String name : allFiles) {
            if (stagingFiles.containsKey(name)) {
                tracked.add(name);
                allNewFiles.put(name, stagingFiles.get(name));
            } else if (!untracked.contains(name)
                    && currentContents.containsKey(name)) {
                tracked.add(name);
                allNewFiles.put(name, currentContents.get(name));
            }
        }
        Commit mergeCommit = new Commit(allNewFiles, current.getHashID(),
                given.getHashID(), currentBranch, branchName);
        stagingArea = new Storage();
        String name = mergeCommit.getHashID();
        allCommits.add(name);
        currentHead = name;
        branchHeads.put(currentBranch, name);
        File mergeCopy = join(GITLET_DIR, name);
        writeObject(mergeCopy, mergeCommit);
        untracked.clear();
    }

    public boolean mergeHelper(HashSet<String> files, HashMap<String, String> gContents,
                               HashMap<String, String> cContents, HashMap<String,
                                String> spContents, String bName, Commit given) {
        boolean conflict = false;
        for (String f : files) {
            if (gContents.containsKey(f)) {
                if (spContents.get(f) != null && !gContents.get(f).equals(spContents.get(f))
                        && cContents.get(f).equals(spContents.get(f))) {
                    fileIDCheckout(f, given.getHashID());
                    stagingArea.insert(f);
                } else if (!spContents.containsKey(f)
                        && !cContents.containsKey(f)) {
                    fileIDCheckout(f, given.getHashID());
                    stagingArea.insert(f);
                } else if (cContents.containsKey(f) && !cContents.get(f).equals(gContents.get(f))) {
                    Blob b1 = readObject(join(GITLET_DIR, cContents.get(f)), Blob.class);
                    Blob b2 = readObject(join(GITLET_DIR, gContents.get(f)), Blob.class);
                    String s = "<<<<<<< HEAD\n" + b1.getWordContents()
                            + "=======\n" + b2.getWordContents() + ">>>>>>>\n";
                    File fFile = new File(f);
                    writeContents(fFile, s);
                    add(f);
                    conflict = true;
                } else if (!cContents.containsKey(f)
                        && !gContents.get(f).equals(spContents.get(f))) {
                    Blob sec = readObject(join(GITLET_DIR, gContents.get(f)), Blob.class);
                    String s = "<<<<<<< HEAD\n" + "=======\n" + sec.getWordContents() + ">>>>>>>\n";
                    File fFile = new File(f);
                    writeContents(fFile, s);
                    add(f);
                    conflict = true;
                }

            } else {
                if (spContents.containsKey(f)) {
                    if (cContents.containsKey(f) && cContents.get(f).equals(spContents.get(f))) {
                        new File(f).delete();
                        untracked.add(f);
                    } else if (cContents.containsKey(f)
                            && !cContents.get(f).equals(spContents.get(f))) {
                        mergeHelperHelper(cContents, bName, f);
                        conflict = true;
                    }
                }
            }
        }
        if (bName.equals("master")) {
            conflict = false;
        }
        return conflict;
    }

    private void mergeHelperHelper(HashMap<String, String> cContents, String bName, String f) {
        Blob b = readObject(join(GITLET_DIR, cContents.get(f)), Blob.class);
        String s = "<<<<<<< HEAD\n" + b.getWordContents() + "=======\n" + ">>>>>>>\n";
        File newFile = new File(f);
        writeContents(newFile, s);
        add(f);
    }

    public String findAncestor(String f1, String f2, String b1, String b2) {
        ancestorHelper(f1, b2, 0);
        ancestorHelper(f2, b1, 0);
        return ancestorHolder;
    }

    public void ancestorHelper(String f, String b, int count) {
        Commit currCommit = readObject(join(GITLET_DIR, f), Commit.class);
        if (currCommit.getBranch().equals(b)) {
            if (count < min) {
                min = count;
                ancestorHolder = f;
            }
        } else if (currCommit.getParent() != null) {
            if (currCommit.getMergeParent() != null) {
                ancestorHelper(currCommit.getMergeParent(), b, count + 1);
            }
            ancestorHelper(currCommit.getParent(), b, count + 1);
        }
    }
}
