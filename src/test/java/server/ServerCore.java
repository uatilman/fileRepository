package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerCore {
    public ServerCore() {
        ServerSocket serverSocket;
        Socket socket;
        try {
            serverSocket = new ServerSocket(8189);
            while (true) {
                System.out.println("wait clients");
                socket = serverSocket.accept();
                System.out.println("client connect");
                new FileThread(socket, null).run();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
