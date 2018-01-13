package client;

import java.io.Externalizable;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;

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

    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    public Message(MessageType messageType, File file) {
        this.messageType = messageType;
        this.file = file;
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
