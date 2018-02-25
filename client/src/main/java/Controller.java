import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;

public class Controller implements Initializable {
    public VBox informationFields;
    public Button buttonRegistration;
    public Button buttonLogin;
    public HBox authorizationField;
    public TextField login;
    public PasswordField password;
    public HBox bottomField;
    public Button buttonSelectFile;
    public Button buttonSelectDir;
    public Button updateButton;
    public Button exitButton;

    private Core core;
    private StartClient app;
    private List<Node> nodeList;


    public void setApp(StartClient app) {
        this.app = app;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorization(false);
    }

    public void setCore(Core core) {
        this.core = core;
    }

    public void setFileViewsList(List<Path> list, Color color) {
        Collections.sort(list);
        Platform.runLater(() -> {
            nodeList = list.stream().map(path -> getSynchronizeLabel(path, color)).collect(Collectors.toList());
            informationFields.getChildren().addAll(nodeList);
        });
    }

    public void printMessage(String text) {
        System.out.println(text);
        Platform.runLater(() -> informationFields.getChildren().add(getSynchronizeLabel(text, GREEN)));
    }

    public void printErrMessage(String text) {
        System.out.println(text);
        Platform.runLater(() -> informationFields.getChildren().add(getSynchronizeLabel(text, RED)));
    }

    private Node getSynchronizeLabel(Path path, Color color) {
        Label label = new Label(path.toString());
        label.setTextFill(color);
        label.setPrefWidth(500);
        label.setPadding(new Insets(0, 0, 10, 0));
        label.setWrapText(true);
        label.setContextMenu(getContextMenu(path));
        return label;
    }

    private ContextMenu getContextMenu(Path path) {
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem itemDeleteFromList = new MenuItem("Удалить из списка синхронизации");
        itemDeleteFromList.setOnAction(e -> core.removePathItem(path, false));

        MenuItem itemDeleteFromDisc = new MenuItem("Удалить c диска");
        itemDeleteFromDisc.setOnAction(e -> core.removePathItem(path, true));

        MenuItem itemShowInExplorer = new MenuItem("Просмотреть в проводнике");
        itemShowInExplorer.setOnAction((ActionEvent e) -> {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + path);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        contextMenu.getItems().addAll(itemDeleteFromList, itemDeleteFromDisc, itemShowInExplorer);
        return contextMenu;
    }

    private Label getSynchronizeLabel(String text, Color color) {
        Label label = new Label(text);
        label.setTextFill(color);
        label.setPadding(new Insets(0, 0, 10, 0));
        label.setWrapText(true);
        return label;
    }

    public void getFilesList() {
        FileChooser fileChooser = new FileChooser();//Класс работы с диалогом выборки и сохранения
        fileChooser.setTitle("Выбирите файлы для синхронизации");//Заголовок диалога
        core.setFiles(fileChooser.showOpenMultipleDialog(app.getPrimaryStage()), null);
    }

    public File getFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        return fileChooser.showOpenDialog(app.getPrimaryStage());
    }

    public void getDir(ActionEvent actionEvent) {
        File file = getDir("Выбирите папки для синхронизации");
        if (file != null)
            core.setFiles(Collections.singletonList(file), null);
    }


    public File getDir(String title) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        return directoryChooser.showDialog(app.getPrimaryStage());
    }

    public void clear() {
        Platform.runLater(() -> informationFields.getChildren().clear());
    }

    public void sendLoginPassword(ActionEvent actionEvent) throws IOException {
        core.sendLogin(login.getText(), password.getText());
    }

    public void setAuthorization(boolean isAuthorization) {
        authorizationField.setVisible(!isAuthorization);
        authorizationField.setManaged(!isAuthorization);
        bottomField.setVisible(isAuthorization);
        bottomField.setManaged(isAuthorization);
        if (isAuthorization)
            Platform.runLater(() -> app.getPrimaryStage().setTitle(core.getLogin().toUpperCase()));
        else Platform.runLater(() -> app.getPrimaryStage().setTitle("not logged in"));
    }

    public void closingWindow() {
        core.closeWindow();
    }

    public void closeWindow() {
        app.primaryStage.close();
    }

    public void registration(ActionEvent actionEvent) {
        app.showPersonEditDialog();
    }

    public void forgotFileDialog(Path path) {
        String fileName = path.toString();
        Platform.runLater(() -> {
            Alert alert = new Alert(CONFIRMATION);
            alert.setTitle("Выбирите действие");
            alert.setWidth(200);
            alert.setHeaderText("В истории синхронизаци обнаружен файл \n\"" + fileName
                    + "\",\n которого нет на диске и сервере.");
            alert.setContentText("Нажмите \"Найти\" для указания пути на файл. \"Удалить\" для удаления истории о файле.");

            ButtonType buttonSave = new ButtonType("Найти");
            ButtonType buttonRemove = new ButtonType("Удалить");
            alert.getButtonTypes().setAll(buttonSave, buttonRemove);

            Optional<ButtonType> result = alert.showAndWait();
            System.out.println(result.get().getText());

            if (result.get() == buttonSave) {
                File file = getFile("Укажите путь к файлу");
                if (file != null) {
                    core.setFiles(Collections.singletonList(file), fileName);
                }
            } else if (result.get() == buttonRemove) {
                core.removeItem(path);
            }

        });
    }


    public void newFileDialog(MyFile myFile)  {
        String fileName = myFile.getFile().toString();
        Platform.runLater(() -> {
            Alert alert = new Alert(CONFIRMATION);
            alert.setTitle("Выбирите действие");
            alert.setHeaderText("На сервере обнаружен файл \n\"" + fileName + "\",\n который отсутствует в вашем списке синхронизации. Сохранить?");
            alert.setContentText("\"Сохранить\" для выбора места сохранения,\n \"Удалить\" для удаления файла с сервера/");

            ButtonType buttonSave = new ButtonType("Сохранить");
            ButtonType buttonRemove = new ButtonType("Удалить");
            alert.getButtonTypes().setAll(buttonSave, buttonRemove);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == buttonSave) {
                File dir = getDir("Выбирите папку для сохранения");
                File newFile = new File(dir.getAbsoluteFile() + "\\" + fileName);
                System.out.println("newFile " + newFile);
                core.setFiles(Collections.singletonList(newFile), fileName);
            } else if (result.get() == buttonRemove) {

                    core.removeServerPath(myFile);

            }
        });

    }

    public void updateFiles(ActionEvent actionEvent) {
        try {
            core.updateFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
