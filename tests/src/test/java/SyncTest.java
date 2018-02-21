import javafx.stage.Stage;
import org.junit.*;

public class SyncTest {

    @Before
    public void init() {
// TODO: 21.02.2018 подготовить папки для сценария
    }

    @Test
    public void serverStart() {
        Thread serverThread = new Thread(() -> ServerStart.main(null));
        serverThread.setDaemon(true);
        serverThread.start();
        try {
            Thread.sleep(1000);
            Assert.assertTrue(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void clientStart() {
        Thread clientThread = new Thread(() -> StartClient.main(null));
        clientThread.setDaemon(true);
        clientThread.start();
        try {
            Thread.sleep(10000);
            Assert.assertTrue(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
