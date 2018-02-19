
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Message implements Serializable {

    private MessageType messageType;

    private String login;
    private String password;

    private byte[] date;
    private MyFile myFile;
    List<MyFile> myFileList;

    public Message(MessageType messageType, MyFile myFile) {
        this.myFile = myFile;
        this.messageType = messageType;
    }

    public List<MyFile> getMyFileList() {
        return myFileList;
    }

    public Message(MessageType messageType, List<MyFile> myFileList) {
        this.myFileList = myFileList;

        this.messageType = messageType;
    }

    public Message(MessageType messageType,  byte[] date, MyFile myFile) { // - String fileName,
        this.myFile = myFile;
        this.messageType = messageType;
        this.date = date;
    }

    public Message(MessageType messageType, String login, String password) {
        this.messageType = messageType;
        this.login = login;
        this.password = password;
    }

    public MyFile getMyFile() {
        return myFile;
    }

    public void sendMessage(ObjectOutputStream os) throws IOException {
        os.writeObject(this);
        os.flush();
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageType=" + messageType +
//                ", file=" + file +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", date=" + Arrays.toString(date) +
                '}';
    }

    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    public byte[] getDate() {
        return date;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

}
