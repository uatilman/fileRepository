import java.io.IOException;

public class Start {
    public static void main(String[] args) {
//        ServerFX.main(args);


        try {
            ServerConsole.main(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
