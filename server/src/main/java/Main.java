import org.apache.commons.codec.digest.DigestUtils;
import sun.misc.BASE64Encoder;
;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
    private static URI SERVER_ADDRESS;


    public static void main(String[] args) throws Exception {
        System.out.println();
        SERVER_ADDRESS = new File("D:\\OneDrive\\programming\\java\\java5\\fileRepository\\serverFiles").toURI();
        File file = new File("D:\\OneDrive\\programming\\java\\java5\\fileRepository\\");

        System.out.println(file.toPath().getName(0));
        System.out.println(file.toPath().getName(1));
        System.out.println(file.toPath().getName(2));
        System.out.println(file.toPath().getName(3));
        System.out.println(file.toPath().getName(4));
/*//        BasicFileAttributes attr;
//        attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
//        System.out.println("Creation time: " + attr.creationTime());
//        System.out.println("Last access time: " + attr.lastAccessTime());
//        System.out.println("Last modified time: " + attr.lastModifiedTime());*/

/*        Map<String, Object> map = Files.readAttributes(file.toPath(), "*", LinkOption.NOFOLLOW_LINKS);

        for (Map.Entry<String, Object> kv : map.entrySet()) {
            System.out.println("key " + kv.getKey() + " --- " + kv.getValue());
        }*/

        System.out.println(file);
        //Тестируем создание дерева файлов

//        System.out.println(paths);

        for (Path f : getPaths(file)) {
            System.out.println(f);
        }


//        File file = new File(SERVER_ADDRESS);
//
//      paths.addAll(paths.stream().filter(path -> Files.isDirectory(path)).collect(Collectors.toList()));
//      paths.forEach(path -> System.out.println(path.getFileName()));


    }

    //TODO ввести в файл на работе
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
