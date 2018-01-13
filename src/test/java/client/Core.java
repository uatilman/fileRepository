package client;


import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Core {
    ObjectOutputStream os;
    ObjectInputStream is;
    Controller controller;
    boolean isAuthorization;

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



        new Thread(() -> {
            controller.printMessage("Авторизируйтесь");
            try {
                do {
                    String s = (String) is.readObject();
                    if (s.startsWith("/authOk")) {
                        setAuthorization();
                    } else {
                        controller.printMessage("Логин или Пароль неверные. Повторите попытку.");
                    }
                } while (true);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Соединение разорвано: " + e.getMessage());
            }
        }).start();
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

}
