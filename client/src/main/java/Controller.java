import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public VBox informationFields;
//    public ListView<Path> fileViewsList;
    public Button buttonRegistration;
    public Button buttonLogin;
    public HBox authorizationField;
    public TextField login;
    public PasswordField password;
    public TextArea textArea;
    public HBox bottomField;
//    public TextField textField;
    public Button buttonSelect;
    public Button applyButton;

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
//        fileViewsList.setItems(fileObservableList);
    }

    public void setCore(Core core) {
        this.core = core;
    }

    public void setFileViewsList(List<Path> list) {

        for (Path path:list) {
            Platform.runLater(() -> {
                Label label = new Label(path.toString());
                label.setTextFill(Color.RED);
                label.setPadding(new Insets(0, 0, 10, 0));
                label.setWrapText(true);
                informationFields.getChildren().add(label);

                // TODO: 15.02.2018
                label.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        System.out.println(  label.getText());
                    }
                });
            });
        }


//        Platform.runLater(() -> {
//            fileObservableList.clear();
//            fileObservableList.addAll(list);
//        });

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

    public void printMessage1(String text) {
        // TODO: 15.02.2018 переделать в всплывающие окна
//        System.out.println(text + "\n");
//        Platform.runLater(() -> textArea.appendText(text + "\n"));
//        if (text != null)

    }

    public void sendLoginPassword(ActionEvent actionEvent) throws IOException {
        core.sendLogin(login.getText(), password.getText());
    }

    public void setAuthorization(boolean isAuthorization) {
        this.isAuthorization = isAuthorization;
        authorizationField.setVisible(!this.isAuthorization);
        authorizationField.setManaged(!this.isAuthorization);
        bottomField.setVisible(this.isAuthorization);
        bottomField.setManaged(this.isAuthorization);
//        textArea.setVisible(this.isAuthorization);
//        fileViewsList.setVisible(this.isAuthorization);
//        fileViewsList.setManaged(this.isAuthorization);
        if (this.isAuthorization) Platform.runLater(() -> app.getPrimaryStage().setTitle(core.getLogin().toUpperCase()));
        else Platform.runLater(() -> app.getPrimaryStage().setTitle("not logged in"));
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



    public void apply(ActionEvent actionEvent) {
    }
}
