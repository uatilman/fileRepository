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
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Controller controller;
    private boolean isAuthorization;
    //    private Thread userThread;
    private List<File> files;
    private final static Path SETTINGS_FILE = Paths.get("properties.txt");
    private List<Path> syncPaths;
    Socket socket = null;


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


    public Core(Controller controller) {
        this.controller = controller;
        isAuthorization = false;
        controller.setCore(this);

        try {

            socket = new Socket("localhost", 8189);
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread userThread = new Thread(() -> {
    controller.printMessage("Авторизируйтесь");
    try {

        while (!isAuthorization) {
            Message message = (Message) is.readObject();
            if (message.getMessageType() == Message.MessageType.AUTHORIZATION) {
                setAuthorization();
            } else {
                controller.printMessage("Логин или Пароль неверные. Повторите попытку.");
            }
        }


        while (true) {
            Message message = (Message) is.readObject();
            switch (message.getMessageType()) {
                case FILE_LIST:
                    List<MyFile> serverFilesList = message.getFiles();
                    List<MyFile> clientFilesList = MyFile.getTree(syncPaths.get(0), syncPaths.get(0));
                    new Thread(() -> synchronize(clientFilesList, serverFilesList)).start();
                    break;
                case FILE:
//                    Path newPath = Paths.get(syncPaths.get(0) + "\\" + message.getFileName());
                    // TODO трогаю, что работает закоментировано, потестирвоать
                    Path newPath = syncPaths.get(0).resolve(message.getMyFile().getFile().toString());

                    if (Files.exists(newPath)) Files.delete(newPath);
                    Files.write(
                            newPath,
                            message.getDate(),
                            StandardOpenOption.CREATE_NEW);
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
            //TODO доделать до полного возобновления + при старте поставить слушатель на ожидание включения сервера, если Соединение разорвано: из за закрытия окна, клоуз тут неприменять
//                        new Core(controller);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        });

//        });
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

        if (myFilesSrc.isEmpty()) return;
        System.out.println("myFilesSrc.size() " + myFilesSrc.size());

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
                    Path path = syncPaths.get(0).resolve(currentSrcFile.getPath());
                    try {
                        Message message = new Message(
                                Message.MessageType.FILE,
                                currentSrcFile.getFile().getName(),
                                Files.readAllBytes(path),
                                currentSrcFile
                        );
                        os.writeObject(message);
                        os.flush();

                    } catch (IOException e) {
                    }

                    //добавляем директорию в список файлов на сервере
                    MyFile mf = MyFile.copy(currentSrcFile);
                    mf.setChildList(new ArrayList<>());
                    myFilesDst.add(mf);
                    synchronize(currentSrcFile.getChildList(), myFilesDst.get(myFilesDst.indexOf(currentSrcFile)).getChildList());
                }

            } else { //если файл

                if (myFilesDst.contains(currentSrcFile)) { //если имена файлов совпадают

                    MyFile currentDstFile = myFilesDst.get(myFilesDst.indexOf(currentSrcFile));

                    if (currentSrcFile.getLastModifiedTime() > currentDstFile.getLastModifiedTime()) { // если у клиента дата последнего изменения больше
                        Path path = syncPaths.get(0).resolve(currentSrcFile.getPath());

                        try {
                            Message message = new Message(
                                    Message.MessageType.FILE,
                                    currentSrcFile.getFile().getName(),
                                    Files.readAllBytes(path),
                                    currentSrcFile
                            );
                            os.writeObject(message);
                            os.flush();

                        } catch (IOException e) {
                        }

                        myFilesSrc.remove(currentSrcFile);
                        myFilesDst.remove(myFilesDst.get(myFilesDst.indexOf(currentSrcFile)));
                        i--;

                    } else if (currentSrcFile.getLastModifiedTime() < currentDstFile.getLastModifiedTime()) { // если у клиента дата последнего изменения меньше
                        //просим файл с сервера
                        try {
                            Message message = new Message(
                                    Message.MessageType.GET,
                                    currentSrcFile
                            );

                            os.writeObject(message);
                            os.flush();
                        } catch (IOException e) {
                        }
                        myFilesSrc.remove(currentSrcFile);
                        myFilesDst.remove(myFilesDst.get(myFilesDst.indexOf(currentSrcFile)));
                        i--;
                    } else {
                        myFilesSrc.remove(currentSrcFile);
                        myFilesDst.remove(myFilesDst.get(myFilesDst.indexOf(currentSrcFile)));
                        i--;
                    }


                } else { // если на сервере файла нет
                    Path path = syncPaths.get(0).resolve(currentSrcFile.getPath());

                    try {
                        Message message = new Message(
                                Message.MessageType.FILE,
                                currentSrcFile.getFile().getName(),
                                Files.readAllBytes(path),
                                currentSrcFile
                        );
                        os.writeObject(message);
                        os.flush();
                    } catch (IOException e) {
                    }

                    myFilesSrc.remove(currentSrcFile);
                    i--;
                    // TODO в текущей версии подразумевается, что в с сервера нельза удалить файл кроме как через единственный клиент,
                    // TODO в противном случае, на сервере нужно вести журнал удалений, и проверять long удаленного файла

                }
            }

        }

        System.out.println("Client");
        for (MyFile mf : myFilesSrc) {
            System.out.println(mf);
        }

        System.out.println("Server");
        //Запрашиваем с сервера недостающие файлы
        for (MyFile mf : myFilesDst) {
            System.out.println(mf);
            try {
                Message message = new Message(
                        Message.MessageType.GET,
                        mf
                );

                os.writeObject(message);
                os.flush();
            } catch (IOException e) {
            }
        }


//            if (myFilesDst.contains(currentFile)) {
//                myFilesDst.remove(currentFile);
//                myFilesSrc.remove(currentFile);
//                System.out.println("remove --- contains");
//
//            } else {
//                if (!currentFile.isDirectory()) {
//                    myFilesDst.add(currentFile);
//                    System.out.println("*****" + currentFile);
//
//
//                    Path path = syncPaths.get(0).resolve(currentFile.getPath());
//
//
//                    Message message = new Message(
//                            Message.MessageType.FILE,
//                            currentFile.getFile().getName(),
//                            Files.readAllBytes(path)
//                    );
//                    os.writeObject(message);
//                    os.flush();
//                    System.out.println("send");
//                } else {
//                    System.out.println("directory\n");
//                }
//                System.out.println("==========================");
//            }


    }

    private void setAuthorization() {
        isAuthorization = true;
        controller.setAuthorization(true);
        controller.printMessage("login ok");
        this.getProperties();

        controller.printMessage("Выбраны папки для синхронизации: ");

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
            socket.close();
            is.close();
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
