import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;

public class MyFile implements Serializable {
    private long lastModifiedTime;
    private transient Path path;
    private List<MyFile> childList;
    private File file;
    private boolean isDirectory;

    public MyFile(Path path, Path root) throws IOException {
        FileTime fileTime = Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS);
        System.out.println(fileTime);
        this.lastModifiedTime = fileTime.toMillis();
        this.isDirectory = Files.isDirectory(path);
        this.path = root.toAbsolutePath().relativize(path.toAbsolutePath());
        this.file = this.path.toFile();
        this.childList = new ArrayList<>();
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public MyFile(long lastModifiedTime, Path path, List<MyFile> childList, File file, boolean isDirectory) {
        this.lastModifiedTime = lastModifiedTime;
        this.path = path;
        this.childList = childList;
        this.file = file;
        this.isDirectory = isDirectory;
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

    public static void print(List<MyFile> test) {
        for (MyFile m : test) {
            System.out.println(m);
            if (m.isDirectory()) {
                print(m.getChildList());
            }
        }
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public static List<MyFile> removeAll(MyFile myFile, Path root) throws Exception {
        List<MyFile> myFiles = MyFile.getTree(root.resolve(myFile.getPath()), root);
        List<MyFile> removeList = new ArrayList<>();
        ListIterator<MyFile> iterator = myFiles.listIterator();

        while (iterator.hasNext() && !myFiles.isEmpty()) {
            MyFile temp = iterator.next();
            try {
                Files.delete(temp.getPath());
                temp.setPath(root.toAbsolutePath().relativize((temp.getPath().toAbsolutePath())));
                removeList.add(temp);//задать относительный путь
                iterator.remove();
            } catch (DirectoryNotEmptyException e) {
                continue;
            }
            if (!iterator.hasNext()) iterator = myFiles.listIterator();
        }
        removeList.add(myFile);
        Files.delete(root.resolve(myFile.getPath()));
        return removeList;
    }

    public static MyFile copy(MyFile src) {
        List<MyFile> childList = new ArrayList<>();
        Collections.copy(childList, src.childList);
        return new MyFile(
                src.lastModifiedTime,
                src.path,
                childList,
                new File(src.file.getPath()),
                src.isDirectory
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
