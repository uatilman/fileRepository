import java.sql.*;


public class SQLHandler {
     Connection connection;
     String url;

    public SQLHandler(String url) {
        this.url = url;
    }

    void connect() throws SQLException {
        connection = DriverManager.getConnection(url);
    }

    void disconnect() throws SQLException {
        connection.close();
    }



}
