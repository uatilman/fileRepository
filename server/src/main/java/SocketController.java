import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketController implements Runnable {
    private ServerController serverController;
    private ServerSocket serverSocket;
    private Socket socket;


    public SocketController(ServerController serverController) {
        this.serverController = serverController;
    }


    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(8189);
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverController.printMessage("wait clients\n");
        try {
            while (serverController.isStatusStart()) {
                socket = serverSocket.accept();
                new Thread(new ClientHandler(socket, serverController)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverController.setStatusStart(false);
        }
    }
}
