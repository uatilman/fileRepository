import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLHandler {
    Connection connection;
    PreparedStatement ps;

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        SQLHandler sqlHandler = new SQLHandler();
        sqlHandler.connection();
        sqlHandler.insertTestDate();
        sqlHandler.disConnect();

    }

    public void insertTestDate() throws SQLException {

//        connection.setAutoCommit(false);
        for (int i = 1; i < 16; i++) {
            ps.setString(1, ("user" + i));
            ps.setString(2, ("pas" + i));
            ps.execute();
//            ps.addBatch();
        }

//        ps.executeBatch();
//        connection.setAutoCommit(true);
    }

    public void connection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:server/users.db");
        ps = connection.prepareStatement("INSERT INTO students (login, password) VALUES (?, ?);");

    }

    public void disConnect() throws ClassNotFoundException, SQLException {
        ps.close();
        connection.close();
    }


}
