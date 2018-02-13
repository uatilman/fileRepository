
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.net.SocketException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Core {
    private boolean isWindowOpen;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Controller controller;
    private boolean isAuthorization;
    private Thread userThread;
    private List<File> files;
    private final static Path SETTINGS_FILE = Paths.get("properties.txt");
    private List<Path> syncPaths;
    private Socket socket = null;


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

    public Thread getUserThread() {
        return userThread;
    }

    public Core(Controller controller) {
        this.controller = controller;
        isAuthorization = false;
        controller.setCore(this);

        //todo
        isWindowOpen = true;

        try {
            socket = new Socket("localhost", 8189);
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.userThread = new Thread(() -> {
            while (isWindowOpen){
                try {
                    if (socket.isClosed()) {
                        socket = new Socket("localhost", 8189);
                        os = new ObjectOutputStream(socket.getOutputStream());
                        is = new ObjectInputStream(socket.getInputStream());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            System.out.println(this.getClass());
            controller.printMessage("Авторизируйтесь");
            try {

                while (!isAuthorization) {
                    Message message = (Message) is.readObject();
                    if (message.getMessageType() == MessageType.AUTHORIZATION_SUCCESSFUL) {
                        setAuthorization(true);
                    } else {
                        controller.printMessage("Логин или Пароль неверные. Повторите попытку.");
                    }
                }
                while (true) {
                    Message message = (Message) is.readObject();
                    switch (message.getMessageType()) {
                        case FILE_LIST:
                            new Thread(() -> synchronize(
                                    MyFile.getTree(syncPaths.get(0), syncPaths.get(0)),
                                    message.getFiles())
                            ).start();
                            break;
                        case FILE:
                            Path newPath = syncPaths.get(0).resolve(message.getMyFile().getFile().toString());
                            if (Files.exists(newPath)) Files.delete(newPath);
                            Files.write(newPath, message.getDate(), StandardOpenOption.CREATE_NEW);
                            Files.setLastModifiedTime(newPath, FileTime.fromMillis(message.getMyFile().getLastModifiedTime()));
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                printException(e);
            } finally {
                try {
                    is.close();
                    os.close();
                    socket.close();
                    setAuthorization(false);
                    //TODO доделать до полного возобновления + при старте поставить слушатель на ожидание включения сервера, если Соединение разорвано: из за закрытия окна, клоуз тут неприменять
                } catch (IOException e) {
                }
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }

        }


        });
        userThread.start();
    }

    private void printException(Exception e) {
        if (e instanceof NoSuchFileException)
            e.printStackTrace();
        else if (e instanceof ClassNotFoundException) {
            System.err.println("Ошибка при передачи сообщений: " + e.getMessage());
        } else if (e instanceof SocketException) {
            System.err.println("Соединение разорвано: " + e.getMessage());
        } else {
            System.err.println("Неопознанная ошибка: " + e.getMessage());
        }
    }

    private void synchronize(List<MyFile> myFilesSrc, List<MyFile> myFilesDst) {
        myFilesSrc.sort(Comparator.comparing(MyFile::getFile));
        myFilesDst.sort(Comparator.comparing(MyFile::getFile));
        for (MyFile mf : myFilesDst) {
            System.out.println("synchronize in " + mf);
        }

        for (int i = 0; i < myFilesSrc.size(); i++) {
            MyFile currentSrcFile = myFilesSrc.get(i);
            if (currentSrcFile.isDirectory()) { //если дирректория
                if (myFilesDst.contains(currentSrcFile)) { // если на сервере есть директория с такимже именем

                    synchronize(currentSrcFile.getChildList(), myFilesDst.get(myFilesDst.indexOf(currentSrcFile)).getChildList());
                    //удаляем файлы из списк синхронизации
                    myFilesSrc.remove(currentSrcFile);
                    myFilesDst.remove(myFilesDst.get(myFilesDst.indexOf(currentSrcFile)));
                    i--;

                } else { //если директории с таким именем нет
                    //отправить информацию о необходимости создать дирректорию
                    sendMessage(new Message(MessageType.DIR, currentSrcFile), "...Send dir " + currentSrcFile);

                    //добавляем директорию в список файлов на сервере
                    MyFile mf = MyFile.copyDir(currentSrcFile);
                    mf.setChildList(new ArrayList<>());
                    myFilesDst.add(mf);
                    synchronize(currentSrcFile.getChildList(), myFilesDst.get(myFilesDst.indexOf(currentSrcFile)).getChildList());
                }

            } else { //если файл

                if (myFilesDst.contains(currentSrcFile)) { //если имена файлов совпадают

                    MyFile currentDstFile = myFilesDst.get(myFilesDst.indexOf(currentSrcFile));

                    if (currentSrcFile.getLastModifiedTime() > currentDstFile.getLastModifiedTime()) { // если у клиента дата последнего изменения больше
                        sendFileMessage(currentSrcFile);

                        myFilesSrc.remove(currentSrcFile);
                        myFilesDst.remove(myFilesDst.get(myFilesDst.indexOf(currentSrcFile)));
                        i--;

                    } else if (currentSrcFile.getLastModifiedTime() < currentDstFile.getLastModifiedTime()) { // если у клиента дата последнего изменения меньше
                        //просим файл с сервера
                        sendMessage(
                                new Message(MessageType.GET, currentSrcFile),
                                "...Get from server file: " + currentSrcFile
                        );

                        myFilesSrc.remove(currentSrcFile);
                        myFilesDst.remove(myFilesDst.get(myFilesDst.indexOf(currentSrcFile)));
                        i--;
                    } else {
                        myFilesSrc.remove(currentSrcFile);
                        myFilesDst.remove(myFilesDst.get(myFilesDst.indexOf(currentSrcFile)));
                        i--;
                    }


                } else { // если на сервере файла нет
                    sendFileMessage(currentSrcFile);
                    myFilesSrc.remove(currentSrcFile);
                    i--;
                    // TODO в текущей версии подразумевается, что в с сервера нельза удалить файл кроме как через единственный клиент,
                    // TODO в противном случае, на сервере нужно вести журнал удалений, и проверять long удаленного файла
                }
            }

        }

        System.out.println("Client");
        for (MyFile mf : myFilesSrc) {
            System.out.println("server....." + mf);
        }

        System.out.println("Server");
        //Запрашиваем с сервера недостающие файлы /создаем папки
        for (MyFile mf : myFilesDst) {
            System.out.println("server....." + mf);
        }

        for (int i = 0; i < myFilesDst.size(); i++) {
            MyFile myCurrentDst = myFilesDst.get(i);


            try {
                if (myCurrentDst.isDirectory()) { // если на сервере есть дирректория, которой нет у клиента рекурсивно синхронизируем все вложения
                    Path newPath = syncPaths.get(0).resolve(myCurrentDst.getFile().toPath());
                    System.out.println(". " + newPath + "\n");
                    deleteIfExists(newPath);
                    Files.createDirectory(newPath);
                    System.out.println("\t\t Create Dir: " + newPath + "\n");

                    MyFile mf = MyFile.copyDir(myCurrentDst);
                    mf.setChildList(new ArrayList<>());
                    myFilesSrc.add(mf);
                    synchronize(myFilesSrc.get(myFilesSrc.indexOf(myCurrentDst)).getChildList(),
                            myCurrentDst.getChildList()
                    );

                } else { // запрашиваем файл с сервера, если его не у клиента
                    sendMessage(
                            new Message(MessageType.GET, myCurrentDst),
                            "...Get from server file: " + myCurrentDst
                    );
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void sendFileMessage(MyFile currentSrcFile) {
        Path path = syncPaths.get(0).resolve(currentSrcFile.getPath());
        try {
            sendMessage(
                    new Message(MessageType.FILE, Files.readAllBytes(path), currentSrcFile),
                    "...Send file " + currentSrcFile
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteIfExists(Path newPath) throws IOException {
        try {
            if (Files.exists(newPath)) {
                System.out.println("delete " + newPath);
                Files.delete(newPath);
            }
        } catch (NoSuchFileException e) {
            System.err.println("NoSuchFileException: " + e.getMessage());
        }
    }

    private void setAuthorization(boolean isAuthorization) {
        this.isAuthorization = isAuthorization;
        controller.setAuthorization(isAuthorization);
        if (isAuthorization) {
            controller.printMessage("login ok");
            this.getProperties();
            controller.printMessage("Выбраны папки для синхронизации: ");
        } else {
            controller.printMessage("Соединение потеряно");
        }
    }

//    public void sendMessage(String text) {
//        try {
//            os.writeObject(text);
//        os.flush();
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
//    }

    public void sendMessage(Message message) {
        try {
            os.writeObject(message);
            os.flush();
            controller.printMessage("Сообщение " + message.getMessageType() + " отправлено ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Message message, String userText) {
        try {
            os.writeObject(message);
            os.flush();
            controller.printMessage(userText);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendLogin(String login, String password) {
        try {
            Message message = new Message(MessageType.GET_AUTHORIZATION, login, password);
            os.writeObject(message);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeWindow() {

        try {
            socket.close();
            is.close();
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        isWindowOpen = false;
    }
}
