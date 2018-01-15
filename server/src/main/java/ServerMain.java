import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerMain extends Application {
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {

        stage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample.fxml"));
        Parent root = loader.load();
        ServerController serverController = loader.getController();
        primaryStage.setTitle("Server");
        primaryStage.setScene(new Scene(root, 500, 300));
        primaryStage.show();
        serverController.printMessage("Welcome to server ");

        ServerCore core = new ServerCore(serverController);
        serverController.setServerCore(core);
        primaryStage.setOnCloseRequest(event -> serverController.closingWindow());
        core.start();

    }

    public static void main(String[] args) {

        launch(args);

    }

}
