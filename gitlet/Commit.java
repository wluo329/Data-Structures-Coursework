package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author William Luo
 */
public class Commit implements Serializable {
    /**
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String commitType;
    private String message;
    private String parent;
    private String branch;
    private String mergeParent;
    private HashMap<String, String> contents;
    private String time;

    //initial commit
    public Commit() {
        this.commitType = "initial";
        this.message = "initial commit";
        this.parent = null;
        this.branch = "master";
        this.mergeParent = null;
        this.contents = new HashMap<String, String>();
        Date date = new Date(0);
        SimpleDateFormat simpleDate = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        this.time = simpleDate.format(date);
    }

    //normal commit
    public Commit(String msg, HashMap<String, String> files, String p, String b) {
        this.commitType = "normal";
        this.message = msg;
        this.parent = p;
        this.branch = b;
        this.mergeParent = null;
        this.contents = files;
        Date date = new Date();
        SimpleDateFormat simpleDate = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        this.time = simpleDate.format(date);
    }

    //commit with merge
    public Commit(HashMap<String, String> files, String p1, String p2, String b1, String b2) {
        this.commitType = "merging";
        this.message  = "Merged " + b2 + " into " + b1 + ".";
        this.parent = p1;
        this.branch = b1;
        this.mergeParent = p2;
        this.contents = files;
        Date date = new Date();
        SimpleDateFormat simpleDate = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        this.time = simpleDate.format(date);
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public String getParent() {
        return parent;
    }

    public String getMergeParent() {
        return mergeParent;
    }

    public String getHashID() {
        ArrayList<Object> uniqueContents = new ArrayList<>();
        uniqueContents.add("Commit");
        uniqueContents.add(commitType);
        uniqueContents.add(message);
        uniqueContents.add(time);
        if (commitType.equals("initial")) {
            return (sha1(uniqueContents));
        } else if (commitType.equals("normal")) {
            uniqueContents.add(parent);
            for (String file: contents.values()) {
                uniqueContents.add(file);
            }
            return (sha1(uniqueContents));
        } else { //merging commit
            uniqueContents.add(parent);
            uniqueContents.add(mergeParent);
            for (String file: contents.values()) {
                uniqueContents.add(file);
            }
            return (sha1(uniqueContents));
        }
    }

    public Collection<String> getBlobs() {
        return contents.values();
    }

    public HashMap<String, String> getContents() {
        return contents;
    }

    public String getBranch() {
        return branch;
    }
}
