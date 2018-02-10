
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ServerControllerFX implements ServerController {
    @FXML
    private TextArea textArea;
    private ServerCore serverCore;

    @Override
    public void clear() {
        textArea.clear();
    }

    @Override
    public void setServerCore(ServerCore serverCore) {
        this.serverCore = serverCore;
    }

    @Override
    public void printMessage(String text) {
        textArea.appendText(text + "\n");
    }

    @Override
    public void close() {
        serverCore.closeWindow();
    }

    @Override
    public void printErrMessage(String message) {
        printMessage(message);
    }
}
