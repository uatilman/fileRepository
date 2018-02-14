import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.IOException;

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

    public void setCore(Core core) {
        this.core = core;
    }

    public void getFilesList() {
        FileChooser fileChooser = new FileChooser();//Класс работы с диалогом выборки и сохранения
        fileChooser.setTitle("Выбирите папку для синхронизации");//Заголовок диалога
        core.setFiles(fileChooser.showOpenMultipleDialog(StartClient.stage));
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
}
