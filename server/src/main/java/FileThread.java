
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.nio.file.*;

import java.nio.file.attribute.FileTime;
import java.sql.SQLException;

import java.util.List;

public class FileThread implements Runnable {
    public Socket socket;
    public List<File> files;
    private String userName;
    private final Path SERVER_ADDRESS = Paths.get("serverFiles");
    private String rootUserDir;
    private ServerStart serverStart;

    public FileThread(Socket socket, ServerStart serverStart) {
        this.socket = socket;
        this.files = files;
        this.serverStart = serverStart;
    }

    @Override
    public void run() {
        System.out.println("run");
        serverStart.printMessage("\tclient connect");
        InputStream in;
        OutputStream os;

        try {
            in = socket.getInputStream();
            os = socket.getOutputStream();
            ObjectInputStream ois = new ObjectInputStream(in);
            ObjectOutputStream oos = new ObjectOutputStream(os);


            while (true) {
                Message message;

                message = (Message) ois.readObject();



                if (message.getMessageType() == MessageType.AUTHORIZATION) {
                    SQLHandler sqlHandler = new SQLHandler();
                    sqlHandler.connect();

                    if (Passwords.isExpectedPassword(
                                            message.getPassword().toCharArray(),
                                            DatatypeConverter.parseHexBinary(sqlHandler.getHash(message.getLogin())),
                                            DatatypeConverter.parseHexBinary(sqlHandler.getSalt(message.getLogin())))
                            ) {
                        this.userName = message.getLogin();
                        serverStart.printMessage("\tclient login as " + userName);

                        oos.writeObject(new Message(MessageType.AUTHORIZATION));
                        oos.flush();

                        rootUserDir = SERVER_ADDRESS + "\\" + userName;
                        Path userPath = Paths.get(rootUserDir);

                        if (!Files.exists(userPath))
                            Files.createDirectory(userPath);
                        Message message1 = new Message(MessageType.FILE_LIST, MyFile.getTree(Paths.get(rootUserDir).toAbsolutePath(), Paths.get(rootUserDir).toAbsolutePath()));
                        oos.writeObject(message1);
                        oos.flush();

                    } else {
                        oos.writeObject("Логин или Пароль неверные. Повторите попытку.");
                        oos.flush();
                    }
                } else if (message.getMessageType() == MessageType.FILE_LIST) {
                } else if (message.getMessageType() == MessageType.DIR) {
                    Path root = Paths.get(rootUserDir).toAbsolutePath();
                    Path newPath = root.resolve(message.getFileName());


                        if (Files.exists(newPath)) {
                            System.out.println("delete " + newPath);
                            Files.delete(newPath);
                            throw new IOException("этого недолжно произойти");
                        }

                    System.out.println("write dir+ " + newPath);


                    Files.createDirectory(newPath);

                } else if (message.getMessageType() == MessageType.FILE) {
                   // TODO везде добавить toAbsolutePath()
                    Path root = Paths.get(rootUserDir).toAbsolutePath();
                    Path newPath = root.resolve(message.getFileName());

//                    Path newPath = Paths.get(rootUserDir + "\\" + message.getFileName());
                    System.out.println("catch file " + newPath + "isDir " + message.getMyFile().isDirectory());
                    try {
                        if (Files.exists(newPath)) {
                            System.out.println("delete " + newPath);
                            Files.delete(newPath);
                        }
                    } catch (NoSuchFileException e) {
                        System.err.println("NoSuchFileException: " + e.getMessage());
                    }



                        System.out.println("write file+ " + newPath);
                        Files.write(
                                newPath,
                                message.getDate(),
                                StandardOpenOption.CREATE);// TODO было CREATE. вернуть, но предварительно удалить файл если он существует


                    Files.setLastModifiedTime(newPath, FileTime.fromMillis(message.getMyFile().getLastModifiedTime()));
                    serverStart.printMessage("\t\t End write file " + message.getFileName());

                } else if (message.getMessageType() == MessageType.DIR) {

                } else if (message.getMessageType() == MessageType.GET) {
                    MyFile getMyFile = message.getMyFile();
                    Path path = Paths.get(rootUserDir + "\\" + getMyFile.getFile().getName());
                    getMyFile.setLastModifiedTime(Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis());

                    Message newMessage = new Message(
                            MessageType.FILE,
                            getMyFile.getFile().getName(),
                            Files.readAllBytes(path),
                            getMyFile
                    );

                    oos.writeObject(newMessage);
                    oos.flush();

                    System.out.println("Файлы отправдены клиенту");
                } else {
                    oos.writeObject("Команда нераспознана");
                    oos.flush();
                }
            }
        } catch (IOException | ClassNotFoundException | SQLException e) {
            String s;
            if (e instanceof EOFException) {
                s = "ObjectInputStream Exception. Connection reset.";
            } else {
                s = e.getMessage();
            }
            serverStart.printErrMessage(s);
            e.printStackTrace();
        }
    }


    public void writeFile(File file) {
        try (FileInputStream fin = new FileInputStream(file);
             FileOutputStream fos = new FileOutputStream(rootUserDir + file.getName())) {
            byte[] buffer = new byte[fin.available()];
            fin.read(buffer, 0, buffer.length);
            fos.write(buffer, 0, buffer.length);
            serverStart.printMessage("\t\t End write file " + file.getName());
        } catch (IOException ex) {
            serverStart.printMessage(ex.getMessage());
        }
    }


}

//