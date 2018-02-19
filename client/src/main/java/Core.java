
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


// TODO: 18.02.2018 отдельные классы на виды сообщений?
// TODO: 18.02.2018 при отправке файла, передавать отдельным полем в сообщении название корневой папки
// TODO: 18.02.2018 сделать синхронизацию для отдельного файла
// TODO: 18.02.2018 после завершение синхроонизации при входе запускать слушателей на обоих сторонах, при любом изменении запускать синхронизацию по отдельной папке (в начале по всему хранилищу)
// TODO: 18.02.2018 список файлов показывать в видоизмененном ListView или в таблице
// TODO: 18.02.2018 drag&drop

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
    private String login;


    Core(Controller controller) {
        this.controller = controller;
        this.isAuthorization = false;
        this.controller.setCore(this);
        this.isWindowOpen = true;

        Thread clientThread = new Thread(() -> {
            while (isWindowOpen) {
                try {
                    if (socket == null || socket.isClosed()) {
                        socket = new Socket("localhost", 8189);
                        os = new ObjectOutputStream(socket.getOutputStream());
                        is = new ObjectInputStream(socket.getInputStream());
                    }
                } catch (IOException e) {
//                    printException(e);
                    controller.printMessage1("Сервер временно недоступен или проблемы с доступом в интернет. Проверьте досуп или повторите попытку позже");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    continue;
                }

                controller.printMessage1("Авторизируйтесь");

                try {

                    while (!isAuthorization) {
                        Message message = (Message) is.readObject();
                        if (message.getMessageType() == MessageType.AUTHORIZATION_SUCCESSFUL) {
                            setAuthorization(true);
                            new Message(MessageType.GET_FILE_LIST).sendMessage(os);
                        } else {
                            controller.printMessage1("Логин или Пароль неверные. Повторите попытку.");
                        }
                    }
                    while (isAuthorization) {
                        Message message = (Message) is.readObject();
                        switch (message.getMessageType()) {
                            case FILE_LIST:
                                try {

                                    List<MyFile> clientList = new ArrayList<>();
                                    for (Path syncPath : syncPaths) {
                                        MyFile myFile = new MyFile(syncPath, syncPath.getParent());
                                        myFile.getChildList().addAll(MyFile.getTree(syncPath, syncPath.getParent()));
                                        clientList.add(myFile);
                                    }
                                    synchronize(clientList, message.getMyFileList());
                                } catch (IOException e) {
                                    printException(e);
                                }
                                break;
                            case FILE:
                                String s = message.getMyFile().getFile().toString();
                                Path path = message.getMyFile().getFile().toPath();
//                                Path rootPath = Paths.get(s.substring(0, s.indexOf("\\")));
//                                Path newPath = syncPaths.get(getIndex(rootPath)).getParent().resolve(path);
                                Path newPath = getAbsolutePath(message.getMyFile());

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
                    } catch (IOException e) {
//                        printException(e);
                    }
                }
            }
        });
        clientThread.setDaemon(true);
        clientThread.start();


    }

    private Integer getIndex(Path rootPath) throws Exception {
        Integer index = null;
        for (int i = 0; i < syncPaths.size(); i++) {
            if (syncPaths.get(i).endsWith(rootPath.toString())) {
                if (index == null) {
                    index = i;
                } else {
                    // TODO: 17.02.2018
                    throw new Exception("Not a unique folder name. The bug will be fixed in the next version");
                }
            }
        }

        return index;
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
//                    MyFile myFile = new MyFile(currentSrcFile.getPath(), currentSrcFile.getPath().getParent());
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
                        //просим файл с сервера

                        new Message(MessageType.GET, currentSrcFile).sendMessage(os);

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

//                Path newPath = root.resolve(myCurrentDst.getFile().toPath());
                Path newPath = getAbsolutePath(myCurrentDst);
                controller.printMessage1(". " + newPath);

                deleteIfExists(newPath);
                Files.createDirectory(newPath);
                controller.printMessage1("Create Dir: " + newPath);
                Files.setLastModifiedTime(newPath, FileTime.fromMillis(myCurrentDst.getLastModifiedTime()));


                MyFile mf = MyFile.copyDir(myCurrentDst);
                mf.setChildList(new ArrayList<>());
                myFilesSrc.add(mf);
                synchronize(myFilesSrc.get(myFilesSrc.indexOf(myCurrentDst)).getChildList(),
                        myCurrentDst.getChildList()
                );
            } else { // запрашиваем файл с сервера, если его не у клиента
                new Message(MessageType.GET, myCurrentDst).sendMessage(os);
            }
        }
    }

    private void sendFileMessage(MyFile currentSrcFile) throws IOException {

        Path path = getAbsolutePath(currentSrcFile);
        new Message(MessageType.FILE, Files.readAllBytes(path), currentSrcFile).sendMessage(os);
    }

    private Path getAbsolutePath(MyFile currentSrcFile) {
        Path currentPath = currentSrcFile.getFile().toPath();
        String relativePath = currentPath.toString();
        int index = relativePath.indexOf("\\");
        String root;
        if (index >= 0) {
            root = relativePath.substring(0, index);
        }else {
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
            controller.printMessage1("delete " + newPath);
            Files.delete(newPath);
        }
    }

    private void setAuthorization(boolean isAuthorization) {
        this.isAuthorization = isAuthorization;
        controller.setAuthorization(isAuthorization);
        controller.clear();

        if (isAuthorization) {

            controller.printMessage1("login ok");
            this.getProperties();
            controller.setFileViewsList(syncPaths);
            controller.printMessage1("Выбраны папки для синхронизации: ");

        } else {
            controller.printMessage1("Соединение потеряно");
        }
    }

//    private void sendMessage(Message message, String userText) throws IOException {
//        os.writeObject(message);
//        os.flush();
//        controller.printMessage1(userText);
//    }

    //TODO https://habrahabr.ru/post/85698/
    public void sendLogin(String login, String password) throws IOException {
        Message message = new Message(MessageType.GET_AUTHORIZATION, login, password);
        this.login = login;
        if (os != null && !socket.isClosed()) {

            os.writeObject(message);
            os.flush();
        } else {
            controller.clear();
            controller.printMessage1("Сервер временно недоступен или проблемы с доступом в интернет. Проверьте досуп или повторите попытку позже");
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
            controller.printMessage1(e.getMessage());
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
                    // TODO: 15.02.2018 пропускать #
                    while (i < settings.size()) {
                        if (settings.get(i).startsWith("#")) {
                            i++;
                            continue;
                        }
                        if ((settings.get(i).startsWith("\\\\") || settings.get(i).substring(1).startsWith(":\\"))) {
                            syncPaths.add(Paths.get(settings.get(i)));
                            i++;
                        }
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
                controller.printMessage1(f.getName());
            }
        }
    }

    private void printException(Exception e) {
        e.printStackTrace();
        if (e instanceof NoSuchFileException) {
            controller.printMessage1("Файл не найден " + e.getMessage());
        } else if (e instanceof ClassNotFoundException) {
            controller.printMessage1("Ошибка при передачи сообщений: " + e.getMessage());
        } else if (e instanceof SocketException) {
            controller.printMessage1("Соединение разорвано: " + e.getMessage());
        } else if (e instanceof IOException) {
            controller.printMessage1(e.getMessage());
        } else {
            controller.printMessage1("Неопознанная ошибка: " + e.getMessage());
        }
    }

}
