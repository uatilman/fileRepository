import java.sql.*;

public class SQLHandler {
    Connection connection;


    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        SQLHandler sqlHandler = new SQLHandler();
        sqlHandler.connection();
//        sqlHandler.insertTestDate();
        System.out.println(sqlHandler.getPassByLogin("user1"));
        sqlHandler.disconnect();

    }
    public String getPassByLogin(String login) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT password FROM users WHERE login = ?");
        ps.setString(1, login);
        ResultSet rs = ps.executeQuery();
        if (rs.next())
            return rs.getString(1);
        return null;
    }
    public void insertTestDate() throws SQLException {
       PreparedStatement ps = connection.prepareStatement("INSERT INTO users (login, password) VALUES (?, ?);");
//        connection.setAutoCommit(false);
        for (int i = 1; i < 16; i++) {
            ps.setString(1, ("user" + i));
            ps.setString(2, ("pas" + i));
            ps.execute();
//            ps.addBatch();
        }
        ps.close();
//        ps.executeBatch();
//        connection.setAutoCommit(true);
    }

    public void connection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:server/fileRepository.db");


    }

    public void disconnect() throws ClassNotFoundException, SQLException {

        connection.close();
    }


}
