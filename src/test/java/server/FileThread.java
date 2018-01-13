package server;

import client.Message;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class FileThread implements Runnable {
    public Socket socket;
    public List<File> files;

    public FileThread(Socket socket, List<File> files) {
        this.socket = socket;
        this.files = files;
    }

    public void run() {
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
                    if (message.getLogin().equals("log") && message.getPassword().equals("pas")) {
                        System.out.println("Клиент залогинился");
                        oos.writeObject("/authOk");
                        oos.flush();
                    }
                } else if (message.getMessageType() == Message.MessageType.FILE_LIST) {

                } else if (message.getMessageType() == Message.MessageType.FILE) {
                    File file = message.getFile();
                    if (file.isDirectory()) {
                        System.out.println(file.getName() + " is Directory");
                        createDirectory(file);
                    } else {
                        writeFile(file);
                    }
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
            System.err.println(e.getMessage());
        }

    }

    public void createDirectory(File file) {

        System.out.println("create dir result " + file.mkdir());

    }

    public void writeFile(File file) {
        try (FileInputStream fin = new FileInputStream(file);
             FileOutputStream fos = new FileOutputStream("src\\test\\java\\server\\" + file.getName())) {
            byte[] buffer = new byte[fin.available()];
            fin.read(buffer, 0, buffer.length);
            fos.write(buffer, 0, buffer.length);
            System.out.println("Закончил писать файл: " + file.getName());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}

//