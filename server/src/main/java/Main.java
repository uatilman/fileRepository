import org.apache.commons.codec.digest.DigestUtils;
import sun.misc.BASE64Encoder;
;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        File file = new File("D:\\OneDrive\\programming\\java\\java5\\fileRepository\\serverFiles");


        System.out.println(file);
        //Тестируем создание дерева файлов

//        System.out.println(paths);

        for (File f : getPaths(file)) {
            System.out.println(f);
        }


//        File file = new File(SERVER_ADDRESS);
//
//      paths.addAll(paths.stream().filter(path -> Files.isDirectory(path)).collect(Collectors.toList()));
//      paths.forEach(path -> System.out.println(path.getFileName()));


    }

    private static List<File> getPaths(File file) throws Exception {
        List<File> files = new ArrayList<>();
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
