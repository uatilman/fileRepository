import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;


public class ClientHandler implements Runnable {
    private Socket socket;
    private String userName;
    private final Path SERVER_ADDRESS = Paths.get("server/serverFiles");
    private String rootUserDir;
    private Path root;
    private ServerController serverController;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean isAuthorise;

    ClientHandler(Socket socket, ServerController serverController) {
        this.socket = socket;
        this.serverController = serverController;
        this.isAuthorise = false;
    }

    @Override
    public void run() {
        serverController.printMessage("\tclient connect from host " + socket.getInetAddress() + "\n");
        try {

            this.in = new ObjectInputStream(socket.getInputStream());
            this.out = new ObjectOutputStream(socket.getOutputStream());

            authorization();
            sendFileListMessage(out);

            while (isAuthorise) {
                Message message;
                message = (Message) this.in.readObject();
                serverController.printMessage("\t Incoming message " + message.getMessageType());

                switch (message.getMessageType()) {
                    case COMMAND_NOT_RECOGNIZED:
                        //TODO add command id - long connection time - increment - toString & server or client
                        break;
                    case FILE_LIST:
                        sendFileListMessage(out);
                        serverController.printMessage("\t\t Files list send\n");
                    case FILE:
                        writeFile(message.getMyFile(), message.getDate());

                        break;
                    case DIR:
                        createDir(message.getMyFile());

                        break;
                    case GET:
                        sendFileMessage(message.getMyFile());

                        break;
                    default:
                        out.writeObject(new Message(MessageType.COMMAND_NOT_RECOGNIZED));
                        out.flush();
                        break;
                }

            }
        } catch (IOException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            isAuthorise = false;
            String s;
            if (e instanceof EOFException) {
                s = "User " + userName + " disconnect";
            } else {
                s = e.getMessage();
            }
            serverController.printErrMessage(s);
        }
    }

    private void writeFile(MyFile newMyFile, byte[] data) throws IOException {
        Path newPath = root.resolve(newMyFile.getFile().toPath());
        serverController.printMessage(". " + newPath + "\n");
        deleteIfExists(newPath);
        Files.write(
                newPath,
                data,
                StandardOpenOption.CREATE);
        Files.setLastModifiedTime(newPath, FileTime.fromMillis(newMyFile.getLastModifiedTime()));
        serverController.printMessage("\t\t File save to disc " + newPath + "\n");
    }

    private void sendFileMessage(MyFile needMyFile) throws IOException {
        serverController.printMessage(". " + needMyFile.getFile().toPath() + "\n");
        Path needPath = root.resolve(needMyFile.getFile().toPath());
        needMyFile.setLastModifiedTime(Files.getLastModifiedTime(needPath, LinkOption.NOFOLLOW_LINKS).toMillis());

        Message newMessage = new Message(
                MessageType.FILE,
                Files.readAllBytes(needPath),
                needMyFile
        );
        out.writeObject(newMessage);
        out.flush();
        serverController.printMessage("\t\t Файлы отправдены клиенту \n");
    }

    private void createDir(MyFile myFile) throws IOException {
        Path newPath = root.resolve(myFile.getFile().toPath());
        deleteIfExists(newPath);
        Files.createDirectory(newPath);
        serverController.printMessage("... Create Dir: " + newPath + "\n");
    }

    private void deleteIfExists(Path newPath) throws IOException {
        try {
            if (Files.exists(newPath)) {
                serverController.printMessage("delete " + newPath);
                Files.delete(newPath);
            }
        } catch (NoSuchFileException e) {
            serverController.printErrMessage("NoSuchFileException: " + e.getMessage());
        }
    }

    private void authorization() throws IOException, ClassNotFoundException, SQLException {
        while (!isAuthorise) {
            Message message;
            message = (Message) in.readObject();

            if (message.getMessageType() == MessageType.GET_AUTHORIZATION) {
                if (SQLHandler.checkPassword(message.getLogin(), message.getPassword())) {
                    sendCommandMessage(MessageType.AUTHORIZATION_SUCCESSFUL);
                    setAuthorise(message.getLogin());
                    if (!isAuthorise) {
                        //TODO Отпправить сообщение о проблемах на сервере
                    }
                } else {
                    sendCommandMessage(MessageType.AUTHORIZATION_FAIL);
                }
            } else {
                sendCommandMessage(MessageType.COMMAND_NOT_RECOGNIZED);
            }
        }
    }

    private void setAuthorise(String login) {
        this.userName = login;
        this.rootUserDir = SERVER_ADDRESS + "/" + userName;
        this.root = Paths.get(rootUserDir).toAbsolutePath();
        this.isAuthorise = true;
        serverController.printMessage("\tclient login as " + userName + "\n");

        if (!Files.exists(root)) {
            try {
                Files.createDirectory(root);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                this.isAuthorise = false;
            }
        }
    }

    private void sendCommandMessage(MessageType messageType) throws IOException {
        out.writeObject(new Message(messageType));
        out.flush();
    }


    private void sendFileListMessage(ObjectOutputStream oos) throws IOException {
        oos.writeObject(
                new Message(MessageType.FILE_LIST, MyFile.getTree(Paths.get(rootUserDir).toAbsolutePath(), Paths.get(rootUserDir).toAbsolutePath()))
        );
        oos.flush();
    }
}

