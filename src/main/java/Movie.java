import java.sql.ResultSet;
import java.sql.SQLException;

public class Movie {
    public String imdbKey;
    public String title;

    public int year;

    public Movie(String imdbKey, String title, int year) {
        this.imdbKey = imdbKey;
        this.title = title;
        this.year = year;
    }

    public static Movie fromRS(ResultSet rs) throws SQLException {
        return new Movie(
                rs.getString("imdb"),
                rs.getString("title"),
                rs.getInt("year")
        );
    }
}
