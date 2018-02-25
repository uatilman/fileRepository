import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;
import org.junit.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class SyncTest {
    Path temp;
    Path client;

    @Before
    public void init() {
//// TODO: 21.02.2018 подготовить папки для сценария
//
//        temp = Paths.get("D:\\OneDrive\\programming\\java\\java5\\fileRepository\\0_temp");
//        client = Paths.get("D:\\OneDrive\\programming\\java\\java5\\fileRepository\\1_clientFiles" + "\\" + temp.getFileName());
//        try {
//           Path p =  Files.walkFileTree(temp, new FileVisitor<Path>() {
//                @Override
//                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                    Path newDir = Paths.get(client + "\\" + dir);
//                    Files.createDirectory(newDir);
//                    return null;
//                }
//
//                @Override
//                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    Path newDir = Paths.get(client + "\\" + dir);
//
//                    Files.copy(file, )
//                    return null;
//                }
//
//                @Override
//                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
//                    return null;
//                }
//
//                @Override
//                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//                    return null;
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    @Test
    public void serverStart() {
        try {
            Thread serverThread = new Thread(() -> ServerStart.main(null));
            serverThread.setDaemon(true);
            serverThread.start();
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void clientStart() {
        try {
            Thread clientThread = new Thread(() -> StartClient.main(null));
            clientThread.setDaemon(true);
            clientThread.start();
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
