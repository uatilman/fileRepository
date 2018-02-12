
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.nio.file.*;

import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class FileThread implements Runnable {
    public Socket socket;
    public List<File> files;
    private String userName;
    private final Path SERVER_ADDRESS = Paths.get("serverFiles");
    private String rootUserDir;
    private ServerCore serverCore;

    public FileThread(Socket socket, ServerCore serverCore) {
        this.socket = socket;
        this.files = files;
        this.serverCore = serverCore;
    }

    @Override
    public void run() {
        System.out.println("run");
        serverCore.printMessage("\tclient connect");
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

                if (message.getMessageType() == Message.MessageType.AUTHORIZATION) {
                    SQLHandler sqlHandler = new SQLHandler();
                    sqlHandler.connect();

                    if (Passwords.isExpectedPassword(
                                            message.getPassword().toCharArray(),
                                            DatatypeConverter.parseHexBinary(sqlHandler.getHash(message.getLogin())),
                                            DatatypeConverter.parseHexBinary(sqlHandler.getSalt(message.getLogin())))
                            ) {
                        this.userName = message.getLogin();
                        serverCore.printMessage("\tclient login as " + userName);

                        oos.writeObject(new Message(Message.MessageType.AUTHORIZATION));
                        oos.flush();
                        rootUserDir = SERVER_ADDRESS + "\\" + userName;
                        Path userPath = Paths.get(rootUserDir);


                        if (!Files.exists(userPath))
                            Files.createDirectory(userPath);
                        Message message1 = new Message(Message.MessageType.FILE_LIST, MyFile.getTree(Paths.get(rootUserDir).toAbsolutePath(), Paths.get(rootUserDir).toAbsolutePath()));
                        oos.writeObject(message1);
                        oos.flush();

                    } else {
                        oos.writeObject("Логин или Пароль неверные. Повторите попытку.");
                        oos.flush();
                    }
                } else if (message.getMessageType() == Message.MessageType.FILE_LIST) {

                } else if (message.getMessageType() == Message.MessageType.FILE) {
                   // TODO везде добавить toAbsolutePath()
                    Path root = Paths.get(rootUserDir).toAbsolutePath();
                    System.out.println("root " + root);
                    Path newPath = root.resolve(message.getMyFile().getFile().toString());

//                    Path newPath = Paths.get(rootUserDir + "\\" + message.getFileName());

                    try {
                        if (Files.exists(newPath)) {
                            System.out.println("delete " + newPath);
                            Files.delete(newPath);

                        }
                    } catch (NoSuchFileException e) {
                        System.err.println("NoSuchFileException: " + e.getMessage());
                    }
                    if (message.getMyFile().isDirectory()){
                        System.out.println("write dir+ " + newPath);

                        Files.createDirectory(newPath);
                    } else {
                        System.out.println("write file+ " + newPath);

                        Files.write(
                                newPath,
                                message.getDate(),
                                StandardOpenOption.CREATE);// TODO было CREATE. вернуть, но предварительно удалить файл если он существует

                    }
                    Files.setLastModifiedTime(newPath, FileTime.fromMillis(message.getMyFile().getLastModifiedTime()));
                    serverCore.printMessage("\t\t End write file " + message.getFileName());

                } else if (message.getMessageType() == Message.MessageType.DIR) {

                } else if (message.getMessageType() == Message.MessageType.GET) {
                    MyFile getMyFile = message.getMyFile();
                    Path path = Paths.get(rootUserDir + "\\" + getMyFile.getFile().getName());
                    getMyFile.setLastModifiedTime(Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis());

                    Message newMessage = new Message(
                            Message.MessageType.FILE,
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
            serverCore.printErrMessage(s);
//            e.printStackTrace();
        }
    }


    public void writeFile(File file) {
        try (FileInputStream fin = new FileInputStream(file);
             FileOutputStream fos = new FileOutputStream(rootUserDir + file.getName())) {
            byte[] buffer = new byte[fin.available()];
            fin.read(buffer, 0, buffer.length);
            fos.write(buffer, 0, buffer.length);
            serverCore.printMessage("\t\t End write file " + file.getName());
        } catch (IOException ex) {
            serverCore.printMessage(ex.getMessage());
        }
    }


}

//