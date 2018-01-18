

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.List;

public class FileThread implements Runnable {
    public Socket socket;
    public List<File> files;
    private String userName;
    private final String SERVER_ADDRESS = "C:\\Users\\uatil\\OneDrive\\programming\\javaGames\\l5\\fileRepository\\serverFiles\\";
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
                Message message = (Message) ois.readObject();
                if (message.getMessageType() == Message.MessageType.AUTHORIZATION) {
                    SQLHandler sqlHandler = new SQLHandler();
                    sqlHandler.connect();

//                    System.out.println(message.getLogin());
//                    System.out.println(message.getPassword());
//                    System.out.println(sqlHandler.getPassByLogin(message.getLogin()));
//                    System.out.println(message.getPassword().equals(sqlHandler.getPassByLogin(message.getLogin())));

                    if (sqlHandler.isPasswordAvalible(message.getLogin(), message.getPassword())) {
                        this.userName = message.getLogin();
                        serverCore.printMessage("\tclient login as " + userName);
                        rootUserDir = SERVER_ADDRESS + userName + "\\";
                        createDirectory(rootUserDir);
                        oos.writeObject("/authOk");
                        oos.flush();
                    } else {
                        oos.writeObject("Логин или Пароль неверные. Повторите попытку.");
                    }
                } else if (message.getMessageType() == Message.MessageType.FILE_LIST) {

                } else if (message.getMessageType() == Message.MessageType.FILE) {
                    Files.write(
                            Paths.get(rootUserDir + message.getFileName()),
                            message.getDate(),
                            StandardOpenOption.CREATE);
                    serverCore.printMessage("\t\t End write file " + message.getFileName());

//                    File file = message.getFile();
//                    if (file.isDirectory()) {
//                        System.out.println(file.getName() + " is Directory");
//                        createDirectory(rootUserDir + file.getName());
//                    } else {
//                        writeFile(file);
//                    }
//
//                    File file = new File("C:\\Users\\usr-mbk00066\\Desktop\\ru.uatilman.fileRepository\\src\\test\\java\\server\\" + message.getPath().toFile().getName());
//
//                    Path path = message.getPath();
//                    FileOutputStream fos = new FileOutputStream(path.toFile());
//                    //TODO уйти от io к nio
//                    File requestFile = message.getPath().toFile();
//                    System.out.printf("Получен файл %s", requestFile.getName());
//                    if (files.contains(requestFile)) {
//                        files.remove(requestFile); //TODO ??? очень странная логика в методичке, удалять существующий файл при получении файла
//                        System.out.printf("Количество объектов после удаления %d", files.size());
//                    } else {
//                        files.add(requestFile);
//                        System.out.printf("Количество объектов после добавления %d", files.size());
//                    }
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
        } catch (IOException | ClassNotFoundException e) {
            serverCore.printMessage(e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public boolean createDirectory(String dirName) {
        return new File(dirName).mkdir();
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