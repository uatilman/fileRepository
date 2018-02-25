
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;

import static java.util.logging.Level.*;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;


// TODO: 18.02.2018 отдельные классы на виды сообщений?
// TODO: 18.02.2018 при отправке файла, передавать отдельным полем в сообщении название корневой папки
// TODO: 18.02.2018 сделать синхронизацию для отдельного файла
// TODO: 18.02.2018 после завершение синхроонизации при входе запускать слушателей на обоих сторонах, при любом изменении запускать синхронизацию по отдельной папке (в начале по всему хранилищу)
// TODO: 18.02.2018 список файлов показывать в видоизмененном ListView или в таблице
// TODO: 18.02.2018 drag&drop

public class Core {
    private boolean isWindowOpen;
    private boolean isAuthorization;

    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Controller controller;
    private List<Path> syncPaths;
    private Socket socket = null;
    private String login;

    private Handler fileHandlerException;
    private Handler fileHandlerInfo;
    private ClientSqlHandler sqlHandler;

    private static final String DB_URL = "jdbc:sqlite:client/client_properties.db";
    private static final Logger LOGGER = Logger.getLogger(Core.class.getName());

    Core(Controller controller) {
        this.controller = controller;
        this.isAuthorization = false;
        this.controller.setCore(this);
        this.isWindowOpen = true;

        initLogger();

        Thread clientThread = new Thread(() -> {
            while (isWindowOpen) {
                try {
                    if (socket == null || socket.isClosed()) {
                        socket = new Socket("localhost", 8189);
                        os = new ObjectOutputStream(socket.getOutputStream());
                        is = new ObjectInputStream(socket.getInputStream());
                        sqlHandler = new ClientSqlHandler(DB_URL);
                    }
                } catch (IOException e) {
                    controller.clear();
                    controller.printErrMessage("Сервер временно недоступен или проблемы с доступом в интернет. Проверьте досуп или повторите попытку позже");
                    LOGGER.log(INFO, "Нет связи с сервером " + e.getMessage());

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        LOGGER.log(WARNING, "", e);
                    }
                    continue;
                }
                controller.clear();
                controller.printMessage("Авторизируйтесь");
                LOGGER.log(INFO, "Связь с сервером установлена");

                try {
                    while (!isAuthorization) {
                        Message message = (Message) is.readObject();
                        if (message.getMessageType() == MessageType.AUTHORIZATION_SUCCESSFUL) {
                            setAuthorization(true);
                            updateFiles();
                        } else {
                            LOGGER.log(INFO, "Логин или Пароль неверные.");
                            controller.printErrMessage("Логин или Пароль неверные. Повторите попытку.");
                        }
                    }

                    LOGGER.log(INFO, "Успешная авторизация");

                    while (isAuthorization) {
                        Message message = (Message) is.readObject();
                        switch (message.getMessageType()) {
                            case FILE_LIST:
                                try {
                                    List<MyFile> serverMyFileList = message.getMyFileList();

                                    List<MyFile> clientList = new ArrayList<>();
                                    for (Path syncPath : syncPaths) {
                                        if (!Files.exists(syncPath)) {
                                            List<Path> serverPathList = serverMyFileList.stream().map(myFile -> myFile.getFile().toPath()).collect(Collectors.toList());

                                            if (!serverPathList.contains(syncPath.getFileName())) { // если файл не пришел с сервера
                                                controller.forgotFileDialog(syncPath);
                                            }
                                            continue;
                                        }
                                        MyFile myFile = new MyFile(syncPath, syncPath.getParent());
                                        myFile.getChildList().addAll(MyFile.getTree(syncPath, syncPath.getParent()));
                                        clientList.add(myFile);
                                    }
                                    synchronize(clientList, serverMyFileList);

                                    controller.clear();
                                    controller.setFileViewsList(syncPaths, GREEN);

                                } catch (IOException e) {
                                    printException(e);
                                }
                                break;
                            case FILE:
                                MyFile myFile = message.getMyFile();
                                Path newPath = getAbsolutePath(message.getMyFile());

                                if (newPath != null) {
                                    if (Files.exists(newPath))
                                        Files.delete(newPath);
                                    Files.write(newPath, message.getDate(), StandardOpenOption.CREATE_NEW);
                                    Files.setLastModifiedTime(newPath, FileTime.fromMillis(message.getMyFile().getLastModifiedTime()));
                                } else { //если корневой файл не упоминается в базе
                                    controller.newFileDialog(myFile);

                                }
                                break;
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
                    } catch (IOException e) {
                    }
                }
            }
        });
        clientThread.setDaemon(true);
        clientThread.start();
    }


    private void initLogger() {
        LOGGER.setLevel(ALL);
        LOGGER.setUseParentHandlers(false);
        try {

            fileHandlerException = new FileHandler("exception.log");
            fileHandlerException.setFormatter(new SimpleFormatter());
            fileHandlerException.setLevel(WARNING);
            LOGGER.addHandler(fileHandlerException);


            fileHandlerInfo = new FileHandler("info.log");
            fileHandlerInfo.setFormatter(new SimpleFormatter());
            fileHandlerInfo.setLevel(CONFIG);
            fileHandlerInfo.setFilter(record -> record.getLevel().equals(INFO));
            LOGGER.addHandler(fileHandlerInfo);


            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(SEVERE);
            LOGGER.addHandler(consoleHandler);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateFiles() throws IOException {
        new Message(MessageType.GET_FILE_LIST).sendMessage(os);
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
                    new Message(MessageType.DIR, currentSrcFile).sendMessage(os);

                    //синхронизируем директорию
                    synchronize(currentSrcFile.getChildList(), new ArrayList<>());
                    removeList.add(currentSrcFile);
                }

            } else { //если файл
                if (myFilesDst.contains(currentSrcFile)) { //если имена файлов совпадают
                    MyFile currentDstFile = myFilesDst.get(myFilesDst.indexOf(currentSrcFile));
                    if (currentSrcFile.getLastModifiedTime() > currentDstFile.getLastModifiedTime()) { // если у клиента дата последнего изменения больше
                        sendFileMessage(currentSrcFile);
                        removeList.add(currentSrcFile);
                    } else if (currentSrcFile.getLastModifiedTime() < currentDstFile.getLastModifiedTime()) { // если у клиента дата последнего изменения меньше
                        getMessage(currentSrcFile);
                        removeList.add(currentSrcFile);
                    } else { // файлы идентичны
                        removeList.add(currentSrcFile);
                    }
                } else { // если на сервере файла нет
                    sendFileMessage(currentSrcFile);
                    removeList.add(currentSrcFile);
                }
            }
        }

        myFilesSrc.removeAll(removeList);
        myFilesDst.removeAll(removeList);
        removeList.clear();

        //Запрашиваем с сервера недостающие файлы /создаем папки
        for (MyFile myCurrentDst : myFilesDst) {
            if (myCurrentDst.isDirectory()) { // если на сервере есть дирректория, которой нет у клиента рекурсивно синхронизируем все вложения
                Path newPath = getAbsolutePath(myCurrentDst);
                if (newPath == null) { // если у клиента папка недобавлена в список синхронизации
                    // TODO: 21.02.2018 в данной версии папка на сервере удаляется. В дальнейшем необходимо запускать диалог на сохранение папки
                    new Message(MessageType.DELETE_FILE, myCurrentDst).sendMessage(os);
                    removeList.add(myCurrentDst);
                    continue;
                }
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
                getMessage(myCurrentDst);
            }
        }
        myFilesDst.removeAll(removeList); //применяем очистку
    }

    private void getMessage(MyFile myCurrentDst) throws IOException {
        new Message(MessageType.GET, myCurrentDst).sendMessage(os);
    }

    private synchronized void sendFileMessage(MyFile currentSrcFile) throws IOException {
        new Message(
                MessageType.FILE,
                Files.readAllBytes(Objects.requireNonNull(getAbsolutePath(currentSrcFile))),
                currentSrcFile).sendMessage(os);
    }

    private Path getAbsolutePath(MyFile currentSrcFile) {
        Path currentPath = currentSrcFile.getFile().toPath();
        String relativePath = currentPath.toString();
        int index = relativePath.indexOf("\\");
        String root;
        if (index >= 0) {
            root = relativePath.substring(0, index);
        } else {
            root = relativePath;
        }

        for (Path path : syncPaths) {
            if (path.toString().endsWith(root)) {
                return path.getParent().resolve(currentPath);
            }
        }
        return null;
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
        controller.clear();

        if (isAuthorization) {
            syncPathsUpdate();
            controller.clear();
            controller.setFileViewsList(syncPaths, GREEN);
        } else {
            controller.printMessage("Соединение потеряно");
            LOGGER.log(INFO, "Соединение потеряно");
        }
    }

    private void syncPathsUpdate() {
        syncPaths = sqlHandler.getPathList();
    }

    public void setFiles(List<File> files, String oldFile) {
        if (files != null) {
            List<Path> paths = files.stream().map(File::toPath).collect(Collectors.toList());
            controller.setFileViewsList(paths, RED);
            if (oldFile != null)
                sqlHandler.remove(oldFile);
            sqlHandler.addPaths(paths);
            syncPathsUpdate();

            try {
                updateFiles();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //TODO https://habrahabr.ru/post/85698/
    public void sendLogin(String login, String password) throws IOException {
        Message message = new Message(MessageType.GET_AUTHORIZATION, login, password);
        this.login = login;
        if (os != null && !socket.isClosed()) {

            os.writeObject(message);
            os.flush();
        } else {
            controller.clear();
            controller.printErrMessage("Сервер временно недоступен или проблемы с доступом в интернет. Проверьте досуп или повторите попытку позже");
        }
    }

    public String getLogin() {
        return login;
    }

    public void closeWindow() {
        try {
            setAuthorization(false);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (is != null) is.close();
            if (os != null) os.close();
            if (socket != null) socket.close();
            isWindowOpen = false;
        } catch (IOException e) {
            controller.printErrMessage(e.getMessage());
            LOGGER.log(INFO, "Окно закрыто");
        }
    }

    public void removeItem(Path path) {
        sqlHandler.remove(path.toString());
    }

    public void removeServerPath(MyFile myFile) {
        try {
            new Message(MessageType.DELETE_FILE, myFile).sendMessage(os);
        } catch (IOException e) {
            printException(e);
        }
    }

    public void removePathItem(Path path, boolean removeFromDisk) {
        removeItem(path);
        syncPaths.remove(path);
        syncPathsUpdate();
        try {
            new Message(MessageType.DELETE_FILE, new MyFile(path, path.getParent())).sendMessage(os);
            if (removeFromDisk)
                Files.delete(path);
            updateFiles();
        } catch (IOException e) {
            printException(e);
        }
        controller.setFileViewsList(syncPaths, GREEN);
    }

    private void printException(Exception e) {
        if (e instanceof NoSuchFileException) {
            controller.printErrMessage("Файл не найден " + e.getMessage());
            LOGGER.log(WARNING, "Файл не найден ", e);
        } else if (e instanceof ClassNotFoundException) {
            controller.printErrMessage("Ошибка при передачи сообщений: " + e.getMessage());
            LOGGER.log(WARNING, "Ошибка при передачи сообщений: ", e);
        } else if (e instanceof SocketException) {
            controller.printErrMessage("Соединение разорвано: " + e.getMessage());
            LOGGER.log(INFO, "Соединение разорвано: ", e);
        } else if (e instanceof IOException) {
            controller.printErrMessage(e.getMessage());
            LOGGER.log(WARNING, "", e);
        } else {
            controller.printErrMessage("Неопознанная ошибка: " + e.getMessage());
            LOGGER.log(WARNING, "Неопознанная ошибка: ", e);
        }
    }
}
