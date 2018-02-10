import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Core {
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Controller controller;
    private boolean isAuthorization;
    private Thread userThread;
    private List<File> files;
    private final static Path SETTINGS_FILE = Paths.get("properties.txt");
    private List<Path> syncPaths;


    public void getProperties() {
        List<String> settings = null;
        try {
            settings = Files.readAllLines(SETTINGS_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        syncPaths = new ArrayList<>();
        for (int i = 0; i < (settings != null ? settings.size() : 0); i++) {
            switch (settings.get(i)) {
                case "[Sync folders]":
                    i++;
                    while (i < settings.size() &&
                            (settings.get(i).startsWith("\\\\") || settings.get(i).substring(1).startsWith(":\\"))) {
                        syncPaths.add(Paths.get(settings.get(i++)));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void setFiles(List<File> files) {
        this.files = files;
        if (files != null) {
            controller.clearTextArea();
            for (File f : files) {
                controller.printMessage(f.getName());

            }
        }
    }

    public void sendFiles() {
        if (files != null) {
            for (File f : files) {
                try {
                    sendMessage(new Message(Message.MessageType.FILE, f.getName(), Files.readAllBytes(Paths.get(f.toURI()))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Core(Controller controller) {
        this.controller = controller;
        isAuthorization = false;
        controller.setCore(this);
        Socket socket = null;
        try {

            socket = new Socket("localhost", 8189);
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Socket socketFinaly = socket;
        userThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                controller.printMessage("Авторизируйтесь");
                try {
                    do {
//                        String s = (String) is.readObject();
                        Message message = (Message) is.readObject();
                        System.out.println(message);
                        if (message.getMessageType() == Message.MessageType.AUTHORIZATION) {
                            break;
                        } else {
//                            controller.printMessage((String) is.readObject());
                            controller.printMessage("Логин или Пароль неверные. Повторите попытку.");
                        }
                    } while (true);

                    setAuthorization();

                    while (true) {
                        Message message = (Message) is.readObject();
                        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        System.out.println(message);
                        switch (message.getMessageType()) {
                            case FILE_LIST:
                                MyFile.print(message.getFiles());
                                break;
                            default:
                                break;
                        }

                    }

                } catch (Exception e) {

                    System.err.println("Соединение разорвано: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    try {
                        is.close();
                        os.close();
                        socketFinaly.close();
                        //TODO доделать до полного возобновления + при старте поставить слушатель на ожидание включения сервера
                        new Core(controller);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        userThread.start();
    }

    private void setAuthorization() {
        isAuthorization = true;
        controller.setAuthorization(true);
        controller.printMessage("login ok");
        this.getProperties();

        controller.printMessage("Выбраны папки для синхронизации: ");
        for (Path p : syncPaths) {
            controller.printMessage(p.toString());
        }
        synchronization();
    }

    public void synchronization() {
        List<MyFile> clientFilesList = MyFile.getTree(syncPaths.get(0), syncPaths.get(0).getFileName());
        MyFile.print(clientFilesList);


    }


    public void sendMessage(String text) {
        try {
            os.writeObject(text);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Message message) {
        try {
            os.writeObject(message);
            os.flush();
            controller.printMessage("Файл " + message.getFileName() + " отправлен ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendLogin(String login, String password) {
        try {
            Message message = new Message(Message.MessageType.AUTHORIZATION, login, password);
            os.writeObject(message);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeWindow() {

        try {
            is.close();
            os.close();
            userThread.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
