import javax.xml.bind.DatatypeConverter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServerSQLHandler extends SQLHandler {

    ServerSQLHandler(String url) {
        super(url);
    }

    public List<String> getUsers() throws SQLException, ClassNotFoundException {
        connect();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT login FROM users");
        List<String> userList = new ArrayList<>();
        while (rs.next()) {
            userList.add(rs.getString(1));
        }
        rs.close();
        statement.close();
        disconnect();
        return userList;
    }

    private String getHash(String login) throws SQLException, ClassNotFoundException {
        connect();

        PreparedStatement ps = connection.prepareStatement("SELECT password_hash  FROM users WHERE login = ?;");
        ps.setString(1, login);
        ResultSet rs = ps.executeQuery();

        String hash = null;
        try {
            hash = rs.getString(1);
        } catch (SQLException e) {

        }

        ps.close();
        disconnect();

        return hash;
    }

    private String getSalt(String login) throws SQLException, ClassNotFoundException {
        connect();
        PreparedStatement ps = connection.prepareStatement("SELECT salt FROM users WHERE login = ?;");
        ps.setString(1, login);
        ResultSet rs = ps.executeQuery();
        String salt = rs.getString(1);
        ps.close();
        disconnect();
        return salt;
    }

    public boolean checkPassword(String login, String password) throws SQLException, ClassNotFoundException {

        boolean result = Passwords.isExpectedPassword(
                password.toCharArray(),
                DatatypeConverter.parseHexBinary(getHash(login)),
                DatatypeConverter.parseHexBinary(getSalt(login))
        );
        return result;
    }


}
