import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;



public class FileThread implements Runnable {
    private Socket socket;
    private String userName;
    private final Path SERVER_ADDRESS = Paths.get("C:\\Users\\uatil\\Desktop\\serverFiles");
    private String rootUserDir;
    private ServerStart serverStart;
    private Path root;

    public FileThread(Socket socket, ServerStart serverStart) {
        this.socket = socket;
        this.serverStart = serverStart;

    }

    @Override
    public void run() {
        serverStart.printMessage("\tclient connect\n");
        InputStream in;
        OutputStream os;

        try {
            in = socket.getInputStream();
            os = socket.getOutputStream();
            ObjectInputStream ois = new ObjectInputStream(in);
            ObjectOutputStream oos = new ObjectOutputStream(os);

            authorization(ois, oos);
            userInit();
            sendFileListMessage(oos);

            while (true) {
                Message message;
                message = (Message) ois.readObject();
                serverStart.printMessage("\t Incoming message " + message.getMessageType());

                switch (message.getMessageType()) {
                    case COMMAND_NOT_RECOGNIZED:
                        //TODO add command id - long connection time - increment - toString & server or client
                        break;
                    case FILE_LIST:
                        sendFileListMessage(oos);
                        serverStart.printMessage("\t\t Files list send\n");
                    case FILE: {
                        Path newPath = root.resolve(message.getMyFile().getFile().toPath());
                        serverStart.printMessage(". " + newPath + "\n");

                        deleteIfExists(newPath);
                        Files.write(
                                newPath,
                                message.getDate(),
                                StandardOpenOption.CREATE);
                        Files.setLastModifiedTime(newPath, FileTime.fromMillis(message.getMyFile().getLastModifiedTime()));
                        serverStart.printMessage("\t\t File save to disc " + newPath + "\n");
                    }
                    break;
                    case DIR: {
                        Path newPath = root.resolve(message.getMyFile().getPath());
                        serverStart.printMessage(". " + newPath + "\n");
                        deleteIfExists(newPath);
                        Files.createDirectory(newPath);
                        serverStart.printMessage("\t\t Create Dir: " + newPath + "\n");
                    }
                    break;
                    case GET: {
                        MyFile needMyFile = message.getMyFile();
                        serverStart.printMessage(". " + message.getMyFile().getFile().toPath() + "\n");


                        Path needPath = root.resolve(message.getMyFile().getFile().toPath());
                        needMyFile.setLastModifiedTime(Files.getLastModifiedTime(needPath, LinkOption.NOFOLLOW_LINKS).toMillis());

                        Message newMessage = new Message(
                                MessageType.FILE,
                                Files.readAllBytes(needPath),
                                needMyFile
                        );
                        oos.writeObject(newMessage);
                        oos.flush();
                        serverStart.printMessage("\t\t Файлы отправдены клиенту \n");
                    }
                    break;
                    default:
                        oos.writeObject(new Message(MessageType.COMMAND_NOT_RECOGNIZED));
                        oos.flush();
                        break;
                }

            }
        } catch (IOException | ClassNotFoundException | SQLException e) {
            String s;
            if (e instanceof EOFException) {
                s = "User " + userName + " disconnect";
            } else {
                s = e.getMessage();
            }
            serverStart.printErrMessage(s);
        }
    }

    private void deleteIfExists(Path newPath) throws IOException {
        try {
            if (Files.exists(newPath)) {
                serverStart.printMessage("delete " + newPath);
                Files.delete(newPath);
            }
        } catch (NoSuchFileException e) {
            serverStart.printErrMessage("NoSuchFileException: " + e.getMessage());
        }
    }

    private void authorization(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException, SQLException {
        while (true) {
            Message message;
            message = (Message) ois.readObject();


            if (message.getMessageType() == MessageType.GET_AUTHORIZATION) {
                SQLHandler sqlHandler = new SQLHandler();
                sqlHandler.connect();

                if (Passwords.isExpectedPassword(
                        message.getPassword().toCharArray(),
                        DatatypeConverter.parseHexBinary(sqlHandler.getHash(message.getLogin())),
                        DatatypeConverter.parseHexBinary(sqlHandler.getSalt(message.getLogin())))
                        ) {
                    this.userName = message.getLogin();
                    serverStart.printMessage("\tclient login as " + userName + "\n");
                    oos.writeObject(new Message(MessageType.AUTHORIZATION_SUCCESSFUL));
                    oos.flush();
                    break;
                } else {
                    oos.writeObject(new Message(MessageType.AUTHORIZATION_FAIL));
                    oos.flush();
                }
                sqlHandler.disconnect();

            } else {
                oos.writeObject(new Message(MessageType.COMMAND_NOT_RECOGNIZED));
                oos.flush();
            }
        }
    }

    private void userInit() throws IOException {
        this.rootUserDir = SERVER_ADDRESS + "\\" + userName;
        this.root = Paths.get(rootUserDir).toAbsolutePath();
        if (!Files.exists(root)) {
            Files.createDirectory(root);
            serverStart.printMessage("\t Create root Directory " + root + "\n");
        } else {
            serverStart.printMessage("\t Root exists " + root + "\n");
        }
    }

    private void sendFileListMessage(ObjectOutputStream oos) throws IOException {
        oos.writeObject(
                new Message(MessageType.FILE_LIST, MyFile.getTree(Paths.get(rootUserDir).toAbsolutePath(), Paths.get(rootUserDir).toAbsolutePath()))
        );
        oos.flush();
    }


    public void writeFile(File file) {
        try (FileInputStream fin = new FileInputStream(file);
             FileOutputStream fos = new FileOutputStream(rootUserDir + file.getName())) {
            byte[] buffer = new byte[fin.available()];
            fin.read(buffer, 0, buffer.length);
            fos.write(buffer, 0, buffer.length);
            serverStart.printMessage("\t\t End write file " + file.getName() + "\n");
        } catch (IOException ex) {
            serverStart.printMessage("Exception " + ex.getMessage() + "\n");
        }
    }


}

//