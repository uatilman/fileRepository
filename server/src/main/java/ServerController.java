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
                ServerSQLHandler handler = new ServerSQLHandler("jdbc:sqlite:server/fileRepository.db");
                try {
                    handler.getUsers().forEach(System.out::println);
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("команда нераспознана");
            }

        }
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

    public void printErrMessage(String message) {
        System.err.println("\n" + message);
    }

}
