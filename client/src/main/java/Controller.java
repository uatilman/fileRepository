import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public VBox informationFields;
    public ListView fileViewsList;
    public Button buttonRegistration;
    public Button buttonLogin;
    public HBox authorizationField;
    public TextField login;
    public PasswordField password;
    public TextArea textArea;
    public HBox bottomField;
    public TextField textField;
    public Button buttonSelect;

    private Core core;
    private boolean isAuthorization;
    private StartClient app;
    private ObservableList<Path> fileObservableList;

    public void setApp(StartClient app) {
        this.app = app;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorization(false);
        fileObservableList = FXCollections.observableArrayList();
        fileViewsList.setItems(fileObservableList);
    }

    public void setCore(Core core) {
        this.core = core;
    }

    public void setFileViewsList(List<Path> list) {
        Platform.runLater(() -> {
            fileObservableList.clear();
            fileObservableList.addAll(list);
        });
    }

    public void getFilesList() {
        FileChooser fileChooser = new FileChooser();//Класс работы с диалогом выборки и сохранения
        fileChooser.setTitle("Выбирите папку для синхронизации");//Заголовок диалога
        core.setFiles(fileChooser.showOpenMultipleDialog(app.getPrimaryStage()));
    }

    public void clearTextArea() {
        textArea.clear();
    }

//    public void sendFiles() {
//        core.sendFiles();
//    }

    public void printMessage(String text) {
//        Platform.runLater(() -> textArea.appendText(text + "\n"));
//        if (text != null)

    }

    public void sendLoginPassword(ActionEvent actionEvent) throws IOException {
        core.sendLogin(login.getText(), password.getText());
    }

    public void setAuthorization(boolean isAuthorization) {
        this.isAuthorization = isAuthorization;
        authorizationField.setVisible(!this.isAuthorization);
        bottomField.setVisible(this.isAuthorization);
//        textArea.setVisible(this.isAuthorization);
    }

    public void closingWindow() {
        core.closeWindow();
    }

    public void registration(ActionEvent actionEvent) {

        app.showPersonEditDialog();
      /*  TextInputDialog dialog = new TextInputDialog("walter");
        dialog.setTitle("Text Input Dialog");
        dialog.setHeaderText("Look, a Text Input Dialog");
        dialog.setContentText("Please enter your name:");

// Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            System.out.println("Your name: " + result.get());
        }

// The Java 8 way to get the response value (with lambda expression).
//        result.ifPresent(name -> System.out.println("Your name: " + name));*/
    }


}
