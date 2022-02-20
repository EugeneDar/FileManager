package sample;

import java.io.File;
import java.net.URI;

public class MyFile extends File {
    public MyFile(String pathname) {
        super(pathname);
    }

    public MyFile(String parent, String child) {
        super(parent, child);
    }

    public MyFile(File parent, String child) {
        super(parent, child);
    }

    public MyFile(URI uri) {
        super(uri);
    }

    public MyFile(File file) {
        super(file.getAbsolutePath());
    }

    @Override
    public String toString() {
        if (super.toString().equals("D:\\")) {
            return "Local Disk (D:)";
        }
        if (super.toString().equals("C:\\")) {
            return "Local Disk (C:)";
        }
        return super.getName();
    }
}
