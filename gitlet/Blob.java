package gitlet;

import java.io.Serializable;
import java.io.File;
import static gitlet.Utils.*;

public class Blob implements Serializable {
    private String name;
    private String blobHash;
    private byte[] byteContents;
    private String wordContents;

    public Blob(String fileName) {
        this.name = fileName;
        File file = new File(fileName);
        if (file.exists()) {
            byteContents = readContents(file);
            wordContents = readContentsAsString(file);
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        blobHash = sha1("Blob", name, byteContents, wordContents);
    }

    byte[] getContents() {
        return byteContents;
    }

    String getBlobHash() {
        return blobHash;
    }

    String getWordContents() {
        return wordContents;
    }
}
