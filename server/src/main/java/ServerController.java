public interface ServerController {

    void clear();

    void setServerCore(ServerCore serverCore);

    void printMessage(String text);

    void close();

    void printErrMessage(String message);
}
