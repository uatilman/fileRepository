import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DialogController {


    public TextField loginField;
    public PasswordField firstPasswordField;
    public PasswordField secondPasswordField;
    private Stage dialogStage;

    public void cancel(ActionEvent actionEvent) {


    }

    public void send(ActionEvent actionEvent) {
        //TODO regex filter
        String login = loginField.getText();
        String password1 = firstPasswordField.getText();
        String password2 = secondPasswordField.getText();
        if (password1.equals(password2)){
            // TODO: 14.02.2018 SQL handler send

        } else {
            // TODO: message dialog
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

}
