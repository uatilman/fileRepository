import org.apache.commons.codec.digest.DigestUtils;

import javax.xml.bind.DatatypeConverter;
import java.sql.*;
import java.util.Arrays;

public class SQLHandler {
    Connection connection;


    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        SQLHandler sqlHandler = new SQLHandler();
        sqlHandler.connect();
        System.out.println(sqlHandler.isPasswordAvalible("user1", "pas1"));
//        sqlHandler.insertTestDate();
//        System.out.println(sqlHandler.getPassByLogin("user1"));
        sqlHandler.disconnect();

    }


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

    public boolean isPasswordAvalible(String login, String password) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT password_hash, salt  FROM users WHERE login = ?");

        ps.setString(1, login);
        ResultSet rs = ps.executeQuery();
        System.out.println("rs1 " + rs.getString(1));
        System.out.println("rs2 " + rs.getString(2));

        String s0 = rs.getString(1);
        byte[] b = DatatypeConverter.parseHexBinary(rs.getString(1));
        String s1 = DatatypeConverter.printHexBinary(b);

        System.out.println("testConvert " + s0);
        System.out.println("testConvert " + s1);


        return rs.next() &&
                Passwords.
                        isExpectedPassword(
                                password.toCharArray(),
                                DatatypeConverter.parseHexBinary(rs.getString(1)),
                                DatatypeConverter.parseHexBinary(rs.getString(2)));
    }

    public void insertTestDate() throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO users (login, password, password_hash, salt) VALUES (?, ?, ?, ?);");
//        connection.setAutoCommit(false);
        String[] strings = null;
        for (int i = 1; i < 16; i++) {
            //TODO add salt см. закладки
            //TODO проверить передачу WireShark

            strings = Passwords.getHashAndSolt("pas" + i);


            ps.setString(1, ("user" + i));
//            ps.setString(2, ( DigestUtils.sha256Hex("pas" + i)));
            ps.setString(2, ("pas" + i));
            ps.setString(3, strings[0]);
            ps.setString(4, strings[1]);
            ps.execute();

//            ps.addBatch();
        }
        Arrays.fill(strings, "*");
        ps.close();
//        ps.executeBatch();
//        connection.setAutoCommit(true);
    }

    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:server/fileRepository.db");


    }

    public void disconnect() throws ClassNotFoundException, SQLException {

        connection.close();
    }


}
