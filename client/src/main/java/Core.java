
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
    private final static Path SETTINGS_FILE = Paths.get("properties.txt");
    private boolean isWindowOpen;
    private boolean isAuthorization;
    private List<File> files; // вероятно пригодится для выбора папок для синхронизации

    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Controller controller;
    private List<Path> syncPaths;
    private Socket socket = null;


    Core(Controller controller) {
        this.controller = controller;
        this.isAuthorization = false;
        this.controller.setCore(this);
        this.isWindowOpen = true;

        try {
            socket = new Socket("localhost", 8189);
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            while (isWindowOpen) {
                try {
                    if (socket.isClosed()) {
                        socket = new Socket("localhost", 8189);
                        os = new ObjectOutputStream(socket.getOutputStream());
                        is = new ObjectInputStream(socket.getInputStream());
                    }
                } catch (IOException e) {
                    printException(e);
                }
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
                    while (isAuthorization) {
                        Message message = (Message) is.readObject();
                        switch (message.getMessageType()) {
                            case FILE_LIST:
                                try {
                                    synchronize(
                                            MyFile.getTree(syncPaths.get(0), syncPaths.get(0)),
                                            message.getFiles());

                                } catch (IOException e) {
                                    printException(e);
                                }
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
                        printException(e);
                    }
                }
            }
        }).start();


    }

    private void synchronize(List<MyFile> myFilesSrc, List<MyFile> myFilesDst) throws IOException {
        myFilesSrc.sort(Comparator.comparing(MyFile::getFile));
        myFilesDst.sort(Comparator.comparing(MyFile::getFile));

        List<MyFile> removeList = new ArrayList<>();
        for (int i = 0; i < myFilesSrc.size(); i++) {
            MyFile currentSrcFile = myFilesSrc.get(i);
            if (currentSrcFile.isDirectory()) { //если дирректория
                if (myFilesDst.contains(currentSrcFile)) { // если на сервере есть директория с такимже именем
                    synchronize(currentSrcFile.getChildList(), myFilesDst.get(myFilesDst.indexOf(currentSrcFile)).getChildList());
                    removeList.add(currentSrcFile);
                } else { //если директории с таким именем нет
                    //отправить информацию о необходимости создать дирректорию
                    sendMessage(new Message(MessageType.DIR, currentSrcFile), "...Send dir " + currentSrcFile);
                    //синхронизируем директорию
                    synchronize(currentSrcFile.getChildList(), new ArrayList<>());
                }

            } else { //если файл

                if (myFilesDst.contains(currentSrcFile)) { //если имена файлов совпадают

                    MyFile currentDstFile = myFilesDst.get(myFilesDst.indexOf(currentSrcFile));

                    if (currentSrcFile.getLastModifiedTime() > currentDstFile.getLastModifiedTime()) { // если у клиента дата последнего изменения больше

                        sendFileMessage(currentSrcFile);
                        removeList.add(currentSrcFile);

                    } else if (currentSrcFile.getLastModifiedTime() < currentDstFile.getLastModifiedTime()) { // если у клиента дата последнего изменения меньше
                        //просим файл с сервера
                        sendMessage(
                                new Message(MessageType.GET, currentSrcFile),
                                "...Get from server file: " + currentSrcFile
                        );
                        removeList.add(currentSrcFile);

                    } else { // файлы идентичны
                        removeList.add(currentSrcFile);
                    }
                } else { // если на сервере файла нет
                    sendFileMessage(currentSrcFile);
                    removeList.add(currentSrcFile);

                    // TODO в текущей версии подразумевается, что в с сервера нельза удалить файл кроме как через единственный клиент,
                    // TODO в противном случае, на сервере нужно вести журнал удалений, и проверять long удаленного файла
                }
            }

        }

        myFilesSrc.removeAll(removeList);
        myFilesDst.removeAll(removeList);

        //Запрашиваем с сервера недостающие файлы /создаем папки
        for (MyFile myCurrentDst : myFilesDst) {
            if (myCurrentDst.isDirectory()) { // если на сервере есть дирректория, которой нет у клиента рекурсивно синхронизируем все вложения
                Path newPath = syncPaths.get(0).resolve(myCurrentDst.getFile().toPath());
                controller.printMessage(". " + newPath);

                deleteIfExists(newPath);
                Files.createDirectory(newPath);
                controller.printMessage("Create Dir: " + newPath);
                Files.setLastModifiedTime(newPath, FileTime.fromMillis(myCurrentDst.getLastModifiedTime()));


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
        }
    }

    private void sendFileMessage(MyFile currentSrcFile) throws IOException {
        Path path = syncPaths.get(0).resolve(currentSrcFile.getPath());
        sendMessage(
                new Message(MessageType.FILE, Files.readAllBytes(path), currentSrcFile),
                "...Send file " + currentSrcFile
        );
    }

    private void deleteIfExists(Path newPath) throws IOException {
        if (Files.exists(newPath)) {
            controller.printMessage("delete " + newPath);
            Files.delete(newPath);
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

    private void sendMessage(Message message, String userText) throws IOException {
        os.writeObject(message);
        os.flush();
        controller.printMessage(userText);
    }

    //TODO https://habrahabr.ru/post/85698/
    public void sendLogin(String login, String password) throws IOException {
        Message message = new Message(MessageType.GET_AUTHORIZATION, login, password);
        os.writeObject(message);
        os.flush();
    }

    public void closeWindow() {
        try {
            socket.close();
            is.close();
            os.close();
            isWindowOpen = false;
        } catch (IOException e) {
            controller.printMessage(e.getMessage());
        }
    }

    private void getProperties() {
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

    private void printException(Exception e) {
        if (e instanceof NoSuchFileException) {
            controller.printMessage("Файл не найден " + e.getMessage());
        } else if (e instanceof ClassNotFoundException) {
            controller.printMessage("Ошибка при передачи сообщений: " + e.getMessage());
        } else if (e instanceof SocketException) {
            controller.printMessage("Соединение разорвано: " + e.getMessage());
        } else if (e instanceof IOException) {
            controller.printMessage(e.getMessage());
        } else {
            controller.printMessage("Неопознанная ошибка: " + e.getMessage());

        }
    }

}
