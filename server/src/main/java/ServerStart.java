
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ServerStart {
    private static boolean isStatusStart;

    public static void main(String[] args) {
        isStatusStart = true;
        ServerStart core = new ServerStart(new ServerController());
        try {
            core.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (isStatusStart) {
            System.out.print("fileServer > ");
            String command = null;
            try {
                command = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (command == null) System.out.println("команда нераспознана");
            if (command.equalsIgnoreCase("users")) {
                try {
                    SQLHandler.connect();
                    SQLHandler.getUsers().forEach(System.out::println);
                    SQLHandler.disconnect();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

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
                while (isStatusStart) {
                    serverController.printMessage("\nwait clients\n");
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
