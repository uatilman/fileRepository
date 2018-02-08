import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerCore {
    private ServerControllerFX serverControllerFX;
    private ServerSocket serverSocket;
    private Socket socket;

    public ServerCore(ServerControllerFX serverControllerFX) {
        this.serverControllerFX = serverControllerFX;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(8189);
        new Thread(() -> {
            try {
                while (true) {
                    serverControllerFX.printMessage("wait clients");
                    socket = serverSocket.accept();
                    new Thread(new FileThread(socket, null, this)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();


    }

    public void printMessage(String message) {
        serverControllerFX.printMessage(message);
    }

    public void closeWindow() {

    }
}
