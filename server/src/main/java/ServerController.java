
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ServerController {
    @FXML
    private TextArea textArea;
    private ServerCore serverCore;

    public void clearTextArea() {
        textArea.clear();
    }

    public void setServerCore(ServerCore serverCore) {
        this.serverCore = serverCore;
    }

    public void printMessage(String text) {
        textArea.appendText(text + "\n");
    }

    public void closingWindow() {
        serverCore.closeWindow();
    }
}
