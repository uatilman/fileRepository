import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;
import java.util.List;


public class ClientHandler implements Runnable {
    private Socket socket;
    private String userName;
    private final Path SERVER_ADDRESS = Paths.get("2_serverFiles");
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

            while (isAuthorise) {
                Message message;
                message = (Message) in.readObject();
                serverController.printMessage("\t Incoming message " + message.getMessageType());

                switch (message.getMessageType()) {
                    case COMMAND_NOT_RECOGNIZED:
                        break;
                    case GET_FILE_LIST:
                        MyFile myFile = new MyFile(Paths.get(rootUserDir), Paths.get(rootUserDir));

                        List<MyFile> mfList = MyFile.getTree(
                                Paths.get(rootUserDir).toAbsolutePath(),
                                Paths.get(rootUserDir).toAbsolutePath());
                        myFile.setChildList(mfList);
                        out.writeObject(new Message(MessageType.FILE_LIST, mfList));
                        out.flush();
                        break;
                    case FILE:
                        writeFile(message.getMyFile(), message.getDate());
                        break;
                    case DIR:
                        createDir(message.getMyFile());
                        break;
                    case GET:
                        sendFileMessage(message.getMyFile());
                        break;
                    case DELETE_FILE:
                        Path deletePath = root.resolve(message.getMyFile().getFile().toPath());
                        deletePath(deletePath);
                        break;
                    default:
                        out.writeObject(new Message(MessageType.COMMAND_NOT_RECOGNIZED));
                        out.flush();
                        break;
                }

            }
        } catch (IOException | ClassNotFoundException | SQLException e) {
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

     private void deletePath(Path path) throws IOException {
        if (!Files.isDirectory(path)) {
            Files.delete(path);
        } else {
            FileUtils.deleteDirectory(path.toFile());
        }
        serverController.printMessage("\t\t Удаляю ...  " + path + "\n");
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
        new Message(MessageType.FILE, Files.readAllBytes(needPath), needMyFile).sendMessage(out);
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
            ServerSQLHandler handler = new ServerSQLHandler("jdbc:sqlite:server/fileRepository.db");
            if (message.getMessageType() == MessageType.GET_AUTHORIZATION) {
                if (handler.checkPassword(message.getLogin(), message.getPassword())) {
                    new Message(MessageType.AUTHORIZATION_SUCCESSFUL).sendMessage(out);
                    setAuthorise(message.getLogin());
                    if (!isAuthorise) {
                        //TODO Отпправить сообщение о проблемах на сервере
                    }
                } else {
                    new Message(MessageType.AUTHORIZATION_FAIL).sendMessage(out);
//                    sendCommandMessage(MessageType.AUTHORIZATION_FAIL);
                }
            } else {
                new Message(MessageType.COMMAND_NOT_RECOGNIZED).sendMessage(out);
//                sendCommandMessage(MessageType.COMMAND_NOT_RECOGNIZED);
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


//    private void sendFileListMessage() throws IOException {
//        out.writeObject(
//                new Message(MessageType.FILE_LIST, MyFile.getTree(Paths.get(rootUserDir).toAbsolutePath(), Paths.get(rootUserDir).toAbsolutePath(), true))
//        );
//        out.flush();
//    }
}

