import myTests.Main;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {
    public enum MessageType{
        AUTHORIZATION,
        FILE_LIST,
        FILE,
        DIR,
        GET
    }

    private MessageType messageType;
    private File file;
    private String login;
    private String password;
    private List<MyFile> files;
    private String fileName;
    private byte[] date;
    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    public List<MyFile> getFiles() {
        return files;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getDate() {
        return date;
    }

    public Message(MessageType messageType, String fileName, byte[] date) {
        this.messageType = messageType;
        this.fileName = fileName;
        this.date = date;
    }

    public Message(MessageType messageType, List<MyFile> files) {
        this.messageType = messageType;
        this.files = files;
    }

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

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setPath(File file) {
        this.file = file;
    }

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
