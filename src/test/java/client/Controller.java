package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;

public class Controller {
    @FXML
    private HBox bottomField;
    @FXML
    private TextField textField;
    @FXML
    private Button button;
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

    public void sendMessage() {
        if (!textField.getText().equals("")) {
            File file = new File("clientFiles\\1.txt");
//            File file1 = new File("2.txt");

//            try {
//                file1.createNewFile();
//                System.out.println(file1.getAbsolutePath());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            Message message = new Message(Message.MessageType.FILE, file);
            core.sendMessage(message);
            textField.clear();
            textField.requestFocus();
        } else {
            textField.requestFocus();
        }
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

}
