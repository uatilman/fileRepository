
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerStart {
    public static void main(String[] args) {
        ServerStart core = new ServerStart(new ServerController());
        try {
            core.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ServerController serverController;
    private ServerSocket serverSocket;
    private Socket socket;

    public ServerStart(ServerController serverController) {
        this.serverController = serverController;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(8189);
        new Thread(() -> {
            try {
                while (true) {
                    serverController.printMessage("wait clients");
                    socket = serverSocket.accept();
                    new Thread(new FileThread(socket, this)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();


    }

    public void printMessage(String message) {
        serverController.printMessage(message);
    }

    public void closeWindow() {

    }

    public void printErrMessage(String message) {
        serverController.printErrMessage(message);
    }
}
