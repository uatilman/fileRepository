import java.io.IOException;

public class ServerControllerConsole implements ServerController {

    @Override
    public void clear() {

    }

    @Override
    public void setServerCore(ServerCore serverCore) {

    }

    @Override
    public void printMessage(String text) {
        System.out.println(text);
    }

    @Override
    public void close() {

    }

}
