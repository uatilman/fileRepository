import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerFX extends Application {
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {

        stage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample.fxml"));
        Parent root = loader.load();
        ServerControllerFX serverControllerFX = loader.getController();
        primaryStage.setTitle("Server");
        primaryStage.setScene(new Scene(root, 500, 300));
        primaryStage.show();
        serverControllerFX.printMessage("Welcome to server ");

        ServerCore core = new ServerCore(serverControllerFX);
        serverControllerFX.setServerCore(core);
        primaryStage.setOnCloseRequest(event -> serverControllerFX.close());
        core.start();

    }

    public static void main(String[] args) {
        launch(args);
    }

}
