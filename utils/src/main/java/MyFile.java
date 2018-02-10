import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;

public class MyFile implements Serializable {
    private Map<String, String> attributeMap;
    private transient Path path;
    private List<MyFile> childList;
    private File file;

    public MyFile(Path path, Path root) throws IOException {
        Map<String, Object> attributeMap = Files.readAttributes(path, "*", LinkOption.NOFOLLOW_LINKS);
        Map<String, String> myAttributeMap = new HashMap<>();

        for (Map.Entry<String, Object> kv : attributeMap.entrySet()) {
            if (kv.getValue() != null)
                myAttributeMap.put(kv.getKey(), kv.getValue().toString());
        }

        this.attributeMap = myAttributeMap;
        this.path = root.toAbsolutePath().relativize(path.toAbsolutePath());
        this.file = this.path.toFile();
        this.childList = new ArrayList<>();
    }


    public List<MyFile> getChildList() {
        return childList;
    }

    public void setChildList(List<MyFile> childList) {
        this.childList = childList;
    }

    //TODO ошибка в преобразовании путей, относитедьные пути исключены



    public static List<MyFile> getTree(Path relativePath, Path root) {
        Path path = root.resolve(relativePath);
        List<MyFile> myFiles = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path p : stream) {
                MyFile myFile = new MyFile(p, root);
                if (Files.isDirectory(p)) {
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

    public Map<String, String> getAttributeMap() {
        return attributeMap;
    }

    public boolean isDirectory() {
        return this.getAttributeMap().get("isDirectory").equals("true");
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\nFile: [").append(file).append("]\n");
        for (Map.Entry<String, String> kv : attributeMap.entrySet()) {
            builder.append(kv.getKey()).append(" --- ").append(kv.getValue()).append("\n");
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }


}
