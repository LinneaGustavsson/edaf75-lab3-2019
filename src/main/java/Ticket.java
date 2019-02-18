import com.google.gson.annotations.JsonAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Ticket {

    public String date;
    public String startTime;
    public String theater;
    public String title;
    public int year;
    public int nbrOfTickets;

    public Ticket(String date, String startTime, String theater, String title, int year, int nbrOfTickets) {
        this.date = date;
        this.startTime = startTime;
        this.theater = theater;
        this.title = title;
        this.year = year;
        this.nbrOfTickets = nbrOfTickets;
    }

    public static Ticket fromRS(ResultSet rs) throws SQLException {
        return new Ticket(
                rs.getString("date"),
                rs.getString("start_time"),
                rs.getString("theatre_name"),
                rs.getString("title"),
                rs.getInt("year"),
                rs.getInt("ticket_count")
        );
    }
}
