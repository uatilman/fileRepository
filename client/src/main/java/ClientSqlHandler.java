import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

class ClientSqlHandler extends SQLHandler {
    ClientSqlHandler(String url) {
        super(url);
    }

    public List<Path> getPathList() {
        try {
            connect();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT path FROM main.paths");

            List<Path> pathList = new ArrayList<>();
            while (rs.next()) {
                pathList.add(Paths.get(rs.getString(1)));
            }
            rs.close();
            statement.close();
            disconnect();
            return pathList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }


    }

    public void remove(String path) {

        try {
            connect();

            PreparedStatement ps = connection.prepareStatement("DELETE FROM paths WHERE path = ?;");
            ps.setString(1, path);
          ps.execute();
            disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void addPaths(List<Path> paths) {
        try {
            connect();
            PreparedStatement ps = connection.prepareStatement("INSERT OR IGNORE INTO main.paths (path) VALUES (?);");
            connection.setAutoCommit(false);
            for (Path path : paths) {
                ps.setString(1, path.toString());
                ps.execute();
            }
            connection.setAutoCommit(true);
            ps.close();
            disconnect();
        } catch (SQLException e) {
            // TODO: 23.02.2018 обработать
            e.printStackTrace();

        }
    }
}

















