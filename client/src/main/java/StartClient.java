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
        Core core = new Core(controller);

        primaryStage.setTitle("File repository");
        primaryStage.setScene(new Scene(root, 500, 300));
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> controller.closingWindow());
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void showPersonEditDialog() {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/dialog.fxml"));
            DialogController controller = loader.getController();


            AnchorPane page = loader.load();
            Stage dialogStage = new Stage();

            dialogStage.setTitle("Registration");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page, 400, 200);
            dialogStage.setScene(scene);

            // Передаём адресата в контроллер.
//            controller.setDialogStage(dialogStage);


            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
