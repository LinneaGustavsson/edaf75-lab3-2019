import java.sql.ResultSet;
import java.sql.SQLException;

public class DBTicket {

    public String ticketId;
    public String userName;
    public String performanceId;

    public DBTicket(String ticketId, String userName, String performanceId) {
        this.ticketId = ticketId;
        this.userName = userName;
        this.performanceId = performanceId;
    }

    public static DBTicket fromRS(ResultSet rs) throws SQLException {
        return new DBTicket(
                rs.getString("ticket_id"),
                rs.getString("user_name"),
                rs.getString("performance_id")
        );
    }
}
