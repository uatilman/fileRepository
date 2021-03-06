package myTests;

import org.apache.commons.codec.digest.DigestUtils;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import java.util.stream.Collectors;

public class Main {
    private static List<Path> paths = new ArrayList<>();
    private static List<Path> paths1 = new ArrayList<>();
    List<MyFile1> myFile1s = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path dir = Paths.get("server\\serverFiles\\user2\\path2\\5");
            WatchKey key = dir.register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path file = ev.context();
                    System.out.println(file + " - " + kind.name());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void streamTest() {
        paths.addAll(paths.stream().filter(path -> Files.isDirectory(path)).collect(Collectors.toList()));
        paths.forEach(path -> System.out.println(path.getFileName()));
    }

    private static void getNameWithIntParamTest(File file) {
        System.out.println(file.toPath().getName(0));
        System.out.println(file.toPath().getName(1));
        System.out.println(file.toPath().getName(2));
        System.out.println(file.toPath().getName(3));
        System.out.println(file.toPath().getName(4));
    }

    private static Map<String, Object> getAttributes(Path path) throws IOException {
        BasicFileAttributes attr;
        attr = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
//        System.out.println("Creation time: " + attr.creationTime());
//        System.out.println("Last access time: " + attr.lastAccessTime());
//        System.out.println("Last modified time: " + attr.lastModifiedTime());

        return Files.readAttributes(path, "*", LinkOption.NOFOLLOW_LINKS);
//        System.err.println(map.getClass());
//        for (Map.Entry<String, Object> kv : map.entrySet()) {
//            System.out.println("key " + kv.getKey() + " --- " + kv.getValue());
//        }

    }

    public static class MyFile1 implements Serializable {
        Map<String, Object> attributeMap;
        String fileName;

        public MyFile1(Map<String, Object> attributeMap, String fileName) {
            this.attributeMap = attributeMap;
            this.fileName = fileName;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("File: [").append(fileName).append("]\n");
            for (Map.Entry<String, Object> kv : attributeMap.entrySet()) {
                builder.append(kv.getKey()).append(" --- ").append(kv.getValue()).append("\n");
            }

            return builder.toString();
        }

        public Map<String, Object> getAttributeMap() {
            return attributeMap;
        }

        public void setAtributeMap(Map<String, Object> atributeMap) {
            this.attributeMap = atributeMap;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public boolean equals(Object obj) {
            MyFile1 myFile1 = (MyFile1) obj;
            return this.getFileName().equals(myFile1.getFileName()) &&
                    (this.getAttributeMap().get("isDirectory").equals(myFile1.getAttributeMap().get("isDirectory")) ||
                            this.getAttributeMap().get("lastModifiedTime").equals(myFile1.getAttributeMap().get("lastModifiedTime")));
        }

        @Override
        public int hashCode() {
            return getFileName().hashCode();
        }
    }

    private static List<Path> getPaths(File file) throws Exception {

        List<Path> paths = new ArrayList<>();

        Files.walkFileTree(file.toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                paths.add(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                paths.add(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });

        return paths;

/* Моя первая реализация обхода дерева
        Queue<File> filesQueue = new PriorityQueue<>(Arrays.asList(Objects.requireNonNull(file.listFiles(File::isDirectory))));
        List<File> files1 = new ArrayList<>(Arrays.asList(Objects.requireNonNull(file.listFiles(pathname -> !pathname.isDirectory()))));

        while (!filesQueue.isEmpty()) {
            File f = filesQueue.remove();
            files.add(f);
            filesQueue.addAll(Arrays.asList(Objects.requireNonNull(f.listFiles(File::isDirectory))));
            files1.addAll(Arrays.asList(Objects.requireNonNull(f.listFiles(pathname -> !pathname.isDirectory()))));
        }
        files.addAll(files1);

        return files;
*/

    }

    private static void testSHA256Variants() throws NoSuchAlgorithmException {
        String text = "hello";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
//        digest.update();

        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
//        new Base64.Decoder().
        System.out.println(new String(hash));
        String encoded = Base64.getMimeEncoder().encodeToString(hash);
        String encoded1 = Base64.getEncoder().encodeToString(hash);
        System.out.println(new BASE64Encoder().encode(hash));
        System.out.println(encoded);
        System.out.println(encoded1);
        System.out.println(bytesToHex(hash));

        System.out.println(DigestUtils.sha256Hex(text));
    }


    private static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }
}
