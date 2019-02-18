import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Calendar;

public class Performance {

    public String theater;
    public String title;
    public int year;
    public String performanceId;
    public String date;
    public String startTime;
    public int remainingSeats;

    public Performance(String theater, String title, int year, String performanceId, String date, String startTime, int remainingSeats) {
        this.theater = theater;
        this.title = title;
        this.year = year;
        this.performanceId = performanceId;
        this.date = date;
        this.startTime = startTime;
        this.remainingSeats = remainingSeats;
    }

    public static Performance fromRS(ResultSet rs) throws SQLException {
        return new Performance(
                rs.getString("theatre_name"),
                rs.getString("title"),
                rs.getInt("year"),
                rs.getString("performance_id"),
                rs.getString("date"),
                rs.getString("start_time"),
                rs.getInt("seats_left")
        );
    }
}
