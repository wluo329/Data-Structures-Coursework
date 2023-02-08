package gitlet;

import java.util.HashMap;
import java.io.File;
import java.io.Serializable;
import static gitlet.Utils.*;

public class Storage implements Serializable {
    private HashMap<String, String> stagedFiles = new HashMap<String, String>();

    public void insert(String fileName) {
        Blob b = new Blob(fileName);
        String blobHash = b.getBlobHash();
        File addedFile = new File(fileName);
        if (addedFile.exists()) {
            if (stagedFiles.containsKey(fileName)) {
                String presentBlob = stagedFiles.get(fileName);
                if (presentBlob != blobHash) {
                    File copy = join(Repository.GITLET_DIR, presentBlob);
                    copy.delete();
                    File file = join(Repository.GITLET_DIR, blobHash);
                    writeObject(file, b);
                }
            } else {
                stagedFiles.put(fileName, blobHash);
                File file = join(Repository.GITLET_DIR, blobHash);
                Utils.writeObject(file, b);
            }
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
        }
    }

    public void remove(String fileName) {
        if (stagedFiles.containsKey(fileName)) {
            String copy = stagedFiles.get(fileName);
            File file = join(Repository.GITLET_DIR, copy);
            file.delete();
            stagedFiles.remove(fileName);
        }
    }

    public boolean contains(String fileName) {
        if (stagedFiles.containsKey(fileName)) {
            return true;
        }
        return false;
    }

    public HashMap<String, String> getContents() {
        return stagedFiles;
    }

    public void clear() {
        stagedFiles.clear();
    }

}
