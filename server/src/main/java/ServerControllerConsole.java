import java.io.IOException;

public class ServerControllerConsole implements ServerController {
    @Override
    public void clear() {
        try {
            Runtime.getRuntime().exec("cls");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setServerCore(ServerCore serverCore) {

    }

    @Override
    public void printMessage(String text) {

    }

    @Override
    public void close() {

    }
}
