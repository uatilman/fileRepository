import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static Stage stage;
//java -jar client-1.0-SNAPSHOT.jar
    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample.fxml"));
        Parent root = loader.load();

        Controller controller = loader.getController();
        Core core = new Core(controller);

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 500, 300));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> controller.closingWindow());
    }


    public static void main(String[] args) {


        launch(args);
    }
}
