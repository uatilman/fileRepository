import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class StartClient extends Application {
    public Stage primaryStage;

    //java -jar client-1.0-SNAPSHOT.jar
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample.fxml"));
        Parent root = loader.load();

        Controller controller = loader.getController();
        controller.setApp(this);
        new Core(controller);

        primaryStage.setTitle("not logged in");
        Scene scene = new Scene(root);
//        Scene scene = new Scene(root, 500, 300);
        scene.getStylesheets().add(0, "my.css");

        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> controller.closingWindow());
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void showPersonEditDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dialog.fxml"));
            AnchorPane page = loader.load();
            Stage newUserDialog = new Stage();

            UserRegistrationController controller = loader.getController();
            controller.setUserRegistrationStage(newUserDialog);

            newUserDialog.setTitle("Registration");
            newUserDialog.initModality(Modality.WINDOW_MODAL); //блокирвока родительсвого окна ниже
            newUserDialog.initOwner(primaryStage); // окно родителя
            newUserDialog.setScene(new Scene(page, 400, 200));

            newUserDialog.showAndWait(); //метод временно блокирует обработку текущего события и запускает вложенный цикл событий для обработки других событий.

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showNewFileDialog() {

    }

    public static void main(String[] args) {
        launch(args);
    }
}
