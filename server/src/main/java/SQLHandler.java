import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLHandler {
    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc.sqlite:main.db");
        PreparedStatement ps = connection.prepareStatement("");
    }
}
