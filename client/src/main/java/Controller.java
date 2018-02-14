import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.util.Optional;

public class Controller {
    @FXML
    private HBox bottomField;
    @FXML
    private TextField textField;
    @FXML
    private Button buttonSelect;
    private Core core;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField login;
    @FXML
    private PasswordField password;
    public Button sendLoginPassword;
    @FXML
    private HBox authorizationField;
    private boolean isAuthorization;
    StartClient app;

    public void setApp(StartClient app) {
        this.app = app;
    }

    public void setCore(Core core) {
        this.core = core;
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
        Platform.runLater(() -> textArea.appendText(text + "\n"));
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
