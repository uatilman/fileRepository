import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;

public class MyFile implements Serializable {
    private transient Path path;
    private File file;
    private File absoluteClientRootFile;
    private long lastModifiedTime;
    private List<MyFile> childList;
    private boolean isDirectory;

    public MyFile(Path path, Path root) throws IOException {
        FileTime fileTime = Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS);
        this.lastModifiedTime = fileTime.toMillis();
        this.isDirectory = Files.isDirectory(path);
        this.path = root.toAbsolutePath().relativize(path.toAbsolutePath());
        this.file = this.path.toFile();
        this.childList = new ArrayList<>();
    }

    private MyFile(long lastModifiedTime, File file, List<MyFile> childList) {
        this.lastModifiedTime = lastModifiedTime;
        this.path = file.toPath();
        this.file = file;
        this.childList = childList;
        this.isDirectory = true;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public boolean isNewer(MyFile reference) {
        return lastModifiedTime > reference.getLastModifiedTime();
    }

    public boolean isOlder(MyFile reference) {
        return lastModifiedTime < reference.getLastModifiedTime();
    }

    public List<MyFile> getChildList() {
        return childList;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public void setChildList(List<MyFile> childList) {
        this.childList = childList;
    }

    public static List<MyFile> getTree(Path relativePath, Path root) {
        Path path = root.resolve(relativePath);
        List<MyFile> myFiles = new ArrayList<>();
        if (!Files.isDirectory(path)) {
            try {
                myFiles.add(new MyFile(path, root));
                return myFiles;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path p : stream) {
                MyFile myFile = new MyFile(p, root);
                if (myFile.isDirectory()) {
                    myFile.childList.addAll(getTree(p, root));
                }
                myFiles.add(myFile);
            }
        } catch (IOException x) {
            x.printStackTrace();
        }
        return myFiles;
    }

    public static MyFile copyDir(MyFile src) {
        List<MyFile> childList = new ArrayList<>(src.childList);
        return new MyFile(
                src.lastModifiedTime,
                src.file,
                childList
        );
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public File getFile() {
        return file;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "MyFile{" +
                "lastModifiedTime=" + lastModifiedTime +
                ", file=" + file +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyFile myFile = (MyFile) o;
        return isDirectory == myFile.isDirectory &&
                Objects.equals(file, myFile.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, isDirectory);
    }
}
