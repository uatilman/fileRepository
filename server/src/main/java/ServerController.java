import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

public class ServerController {
    private SocketController socketController;
    private boolean isStatusStart;

    public ServerController() {
        this.isStatusStart = true;
        this.socketController = new SocketController(this);
        socketController.run();

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
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("команда нераспознана");
            }

        }
    }

    public void clear() {

    }

    public void setServerCore(ServerStart serverStart) {

    }

    public boolean isStatusStart() {
        return isStatusStart;
    }

    public void setStatusStart(boolean isStatusStart) {
        this.isStatusStart = isStatusStart;
    }

    public void printMessage(String text) {
        System.out.print(text);

    }

    public void close() {

    }

    public void printErrMessage(String message) {
        System.err.println(message);
    }

}
