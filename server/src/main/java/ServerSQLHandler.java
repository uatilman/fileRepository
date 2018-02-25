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
        connect();
        boolean result = Passwords.isExpectedPassword(
                password.toCharArray(),
                DatatypeConverter.parseHexBinary(getHash(login)),
                DatatypeConverter.parseHexBinary(getSalt(login))
        );
        disconnect();
        return result;
    }

    public void insertTestDate() throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO users (login, password, password_hash, salt) VALUES (?, ?, ?, ?);");
        String[] strings = null;
        for (int i = 1; i < 16; i++) {
            strings = Passwords.getHashAndSalt("pas" + i);
            ps.setString(1, ("user" + i));
            ps.setString(2, ("pas" + i));
            ps.setString(3, strings[0]);
            ps.setString(4, strings[1]);
            ps.execute();
        }
        ps.close();

    }


}
