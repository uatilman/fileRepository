package client;


import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Core {
    ObjectOutputStream os;
    ObjectInputStream is;
    Controller controller;
    boolean isAuthorization;
    Thread userThread;
    private List<File> files;


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
        System.out.println("list " + files);
        if (files != null) {
            for (File f : files) {
                try {
                    sendMessage(new Message(Message.MessageType.FILE, f.getName(), Files.readAllBytes(Paths.get(f.toURI()))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


//        if (!textField.getText().equals("")) {
//            File file = new File("clientFiles\\1.txt");
////            File file1 = new File("2.txt");
//
////            try {
////                file1.createNewFile();
////                System.out.println(file1.getAbsolutePath());
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//
//            core.sendMessage(message);
//            textField.clear();
//            textField.requestFocus();
//        } else {
//            textField.requestFocus();
//        }
    }

    public Core(final Controller controller) {
        this.controller = controller;
        isAuthorization = false;
        controller.setCore(this);
        try {
            Socket socket = new Socket("localhost", 8189);
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }


        userThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                controller.printMessage("Авторизируйтесь");
                try {
                    do {
                        String s = (String) is.readObject();
                        if (s.startsWith("/authOk")) {
                            Core.this.setAuthorization();
                        } else {
                            controller.printMessage("Логин или Пароль неверные. Повторите попытку.");
                        }
                    } while (true);

                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Соединение разорвано: " + e.getMessage());
                }

            }
        });
        userThread.start();
    }

    private void setAuthorization() {
        isAuthorization = true;
        controller.setAuthorization(true);
        controller.printMessage("login ok");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendLogin(String login, String password) {
        try {
            client.Message message = new client.Message(client.Message.MessageType.AUTHORIZATION, login, password);
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
