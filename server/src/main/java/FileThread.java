
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.sql.SQLException;

import java.util.List;

public class FileThread implements Runnable {
    public Socket socket;
    public List<File> files;
    private String userName;
    private final Path SERVER_ADDRESS = Paths.get("D:\\OneDrive\\programming\\java\\java5\\fileRepository\\serverFiles");
    private String rootUserDir;
    private ServerCore serverCore;

    public FileThread(Socket socket, List<File> files, ServerCore serverCore) {
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

                    if (
                            Passwords.
                                    isExpectedPassword(
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
                        System.err.println("+++" + Paths.get(rootUserDir));
                        Message message1 = new Message(Message.MessageType.FILE_LIST, MyFile.getTree(Paths.get(rootUserDir), Paths.get(rootUserDir)));
                        oos.writeObject(message1);
                        oos.flush();

                    } else {
                        oos.writeObject("Логин или Пароль неверные. Повторите попытку.");
                    }
                } else if (message.getMessageType() == Message.MessageType.FILE_LIST) {

                } else if (message.getMessageType() == Message.MessageType.FILE) {
                    System.out.println("fiiiiiiiiiiiile ");
                    System.out.println(message.getFileName());
                    Files.write(
                            Paths.get(rootUserDir + "\\" + message.getFileName()),
                            message.getDate(),
                            StandardOpenOption.CREATE);
                    serverCore.printMessage("\t\t End write file " + message.getFileName());

                } else if (message.getMessageType() == Message.MessageType.DIR) {

                } else if (message.getMessageType() == Message.MessageType.GET) {
                    oos.writeObject(files);
                    oos.flush();

                    System.out.printf("Файлы отправдены клиенту");
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
            e.printStackTrace();
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