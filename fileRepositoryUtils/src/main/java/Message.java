
import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Message implements Serializable {

    private MessageType messageType;
//    private File file;
//    private String fileName;

    private String login;
    private String password;
    private List<MyFile> files;

    private byte[] date;
    private MyFile myFile;

    public MyFile getMyFile() {
        return myFile;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageType=" + messageType +
//                ", file=" + file +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", files=" + files +
//                ", fileName='" + fileName + '\'' +
                ", date=" + Arrays.toString(date) +
                '}';
    }

//    public Message(MessageType messageType, String fileName) {
//        this.messageType = messageType;
//        this.fileName = fileName;
//    }

    public Message(MessageType messageType) {
        this.messageType = messageType;

    }

    public List<MyFile> getFiles() {
        return files;
    }

//    public String getFileName() {
//        return fileName;
//    }

    public byte[] getDate() {
        return date;
    }



    public Message(MessageType messageType,  byte[] date, MyFile myFile) { // - String fileName,
        this.myFile = myFile;
        this.messageType = messageType;
//        this.fileName = fileName;
        this.date = date;
    }

    public Message(MessageType messageType, MyFile myFile) {
        this.myFile = myFile;
        this.messageType = messageType;
//        this.fileName = fileName;
        this.date = date;
    }

//    public Message(MessageType messageType, MyFile myFile, List<MyFile> files) {
//        this.messageType = messageType;
//        this.files = files;
//    }

    public Message(MessageType messageType, String login, String password) {
        this.messageType = messageType;
        this.login = login;
        this.password = password;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

//    public File getFile() {
//        return file;
//    }
//
//    public void setFile(File file) {
//        this.file = file;
//    }
//
//    public void setPath(File file) {
//        this.file = file;
//    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
