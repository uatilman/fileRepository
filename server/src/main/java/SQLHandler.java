import java.sql.*;

public class SQLHandler {
    private Connection connection;

//    public static void main(String[] args) throws SQLException, ClassNotFoundException {
//        SQLHandler sqlHandler = new SQLHandler();
//        sqlHandler.connect();
////        System.out.println(sqlHandler.isPasswordAvalible("user1", "pas1"));
////        sqlHandler.insertTestDate();
////        System.out.println(sqlHandler.getPassByLogin("user1"));
//        sqlHandler.disconnect();
//
//    }

    public String getHash(String login) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT password_hash  FROM users WHERE login = ?");
        ps.setString(1, login);
        ResultSet rs = ps.executeQuery();
        String hash = rs.getString(1);
        ps.close();
        return hash;
    }

    public String getSalt(String login) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT salt  FROM users WHERE login = ?");

        ps.setString(1, login);
        ResultSet rs = ps.executeQuery();
        String salt = rs.getString(1);
        ps.close();
        return salt;
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

    public void connect() throws ClassNotFoundException, SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:server/fileRepository.db");
    }

    public void disconnect() throws ClassNotFoundException, SQLException {
        connection.close();
    }


}
