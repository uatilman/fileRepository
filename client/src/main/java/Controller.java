import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

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
        fileChooser.setTitle("Выбирите файлы для отправки");//Заголовок диалога
        core.setFiles(fileChooser.showOpenMultipleDialog(Main.stage));
    }

    public void clearTextArea() {
        textArea.clear();
    }

    public void sendFiles() {
        core.sendFiles();
    }

    public void printMessage(String text) {
        textArea.appendText(text + "\n");
    }

    public void sendLoginPassword(ActionEvent actionEvent) {
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
