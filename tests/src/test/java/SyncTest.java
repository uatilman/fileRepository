import javafx.stage.Stage;
import org.junit.*;

public class SyncTest {

    @Before
    public void start() {
        new Thread(() -> ServerStart.main(null)).start();
        new Thread(() -> StartClient.main(null)).start();
    }

    @Test
    public void testTest() {
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertTrue("ddd", true);
    }

}
