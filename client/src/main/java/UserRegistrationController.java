import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UserRegistrationController {
    public TextField loginField;
    public PasswordField firstPasswordField;
    public PasswordField secondPasswordField;
    private Stage userRegistrationStage;

    public void cancel(ActionEvent actionEvent) {
        userRegistrationStage.close();

    }

    public void send(ActionEvent actionEvent) {
        //TODO regex filter
        String login = loginField.getText();
        String password1 = firstPasswordField.getText();
        String password2 = secondPasswordField.getText();
        if (password1.equals(password2)) {
            // TODO: 14.02.2018 SQL handler send
            System.out.println(login);
        } else {
            // TODO: message dialog
        }
    }

    public void setUserRegistrationStage(Stage userRegistrationStage) {
        this.userRegistrationStage = userRegistrationStage;
    }

}
