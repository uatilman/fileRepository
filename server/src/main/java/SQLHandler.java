import javax.xml.bind.DatatypeConverter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLHandler {
    private static Connection connection;

//    public static void main(String[] args) throws SQLException, ClassNotFoundException {
//        SQLHandler sqlHandler = new SQLHandler();
//        sqlHandler.connect();
////        System.out.println(sqlHandler.isPasswordAvalible("user1", "pas1"));
////        sqlHandler.insertTestDate();
////        System.out.println(sqlHandler.getPassByLogin("user1"));
//        sqlHandler.disconnect();
//
//    }

    public static List<String> getUsers() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT login FROM users");
        List<String> userList = new ArrayList<>();
        while (rs.next()) {
            userList.add(rs.getString(1));
        }
        rs.close();
        statement.close();
        return userList;
    }

    public static String getHash(String login) throws SQLException, ClassNotFoundException {
        connect();

        PreparedStatement ps = connection.prepareStatement("SELECT password_hash  FROM users WHERE login = ?;");
        ps.setString(1, login);
        ResultSet rs = ps.executeQuery();
        String hash = rs.getString(1);
        ps.close();
        disconnect();

        return hash;
    }

    public static String getSalt(String login) throws SQLException, ClassNotFoundException {
        connect();
        PreparedStatement ps = connection.prepareStatement("SELECT salt FROM users WHERE login = ?;");
        ps.setString(1, login);
        ResultSet rs = ps.executeQuery();
        String salt = rs.getString(1);
        ps.close();
        disconnect();
        return salt;
    }

    public static boolean  checkPassword(String login, String password) throws SQLException, ClassNotFoundException {
        return Passwords.isExpectedPassword(
                password.toCharArray(),
                DatatypeConverter.parseHexBinary(SQLHandler.getHash(login)),
                DatatypeConverter.parseHexBinary(SQLHandler.getSalt(login))
        );
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

    public static void connect() throws ClassNotFoundException, SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:../server/fileRepository.db");
    }

    public static void disconnect() throws ClassNotFoundException, SQLException {
        connection.close();
    }


}
