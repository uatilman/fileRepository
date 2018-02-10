import java.io.IOException;

public class ServerConsole {

    public static void main(String[] args) throws IOException {
        ServerCore core = new ServerCore(new ServerControllerConsole());
        core.start();
    }

}
