import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private Connection conn;

    public Database() {
        conn = null;
    }

    public void connect() {
        if (conn != null) {
            return;
        }

        try {
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            conn = DriverManager.getConnection("jdbc:sqlite:movies.db", config.toProperties());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setValues(PreparedStatement statement, Object[] arguments) throws SQLException {
        for (int i = 0; i < arguments.length; i++) {
            Object arg = arguments[i];
            int pos = i + 1;
            if (arg instanceof String) {
                statement.setString(pos, (String) arg);
            } else if (arg instanceof Integer) {
                statement.setInt(pos, (Integer) arg);
            } else {
                statement.setObject(pos, arg);
            }
        }
    }

    private int executeUpdateStatement(String sql, Object... arguments) {
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            setValues(statement, arguments);
            return statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private boolean executeStatement(String sql, Object... arguments) {
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            setValues(statement, arguments);
            return statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private <R> List<R> executeQueryStatement(
            String sql,
            ThrowingFunction<ResultSet, R> objFromRS,
            Object... arguments
    ) {
        List<R> result = new ArrayList<>();
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            setValues(statement, arguments);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                result.add(objFromRS.apply(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void resetDB() {
        clearDB();

        insertUser("alice", "Alice", "dobido");
        insertUser("bob", "Bob", "whatsinaname");

        insertMovie("The Shape of Water", 2017, "tt5580390");
        insertMovie("Moonlight", 2016, "tt4975722");
        insertMovie("Spotlight", 2015, "tt1895587");
        insertMovie("Birdman", 2014, "tt2562232");

        insertTheaters("Kino", 10);
        insertTheaters("Södran", 16);
        insertTheaters("Skandia", 100);
    }

    private void clearDB() {
        executeStatement("PRAGMA foreign_keys = OFF;");
        executeStatement("DELETE FROM customers;");
        executeStatement("DELETE FROM theatres;");
        executeStatement("DELETE FROM movies;");
        executeStatement("DELETE FROM tickets;");
        executeStatement("DELETE FROM performances;");
        executeStatement("PRAGMA foreign_keys = ON;");
    }

    private void insertTheaters(String name, int capacity) {
        String sql = "INSERT "
                + "INTO theatres(theatre_name, capacity)"
                + "VALUES (?, ?)";

        executeStatement(sql, name, capacity);
    }

    public boolean insertUser(String userName, String name, String password) {
        String sql = "INSERT "
                + "INTO customers(user_name, full_name, password)"
                + "VALUES (?, ?, ?)";

        return executeStatement(sql, userName, name, PasswordHashGenerator.hash(password));
    }

    public boolean insertMovie(String title, int year, String imdb) {
        String sql = "INSERT "
                + "INTO movies(imdb, year, title, duration)"
                + "VALUES (?, ?, ?, 0)";

        return executeStatement(sql, imdb, year, title);
    }

    public List<Movie> getMovies(String title, Integer year) {
        String query = "SELECT * "
                + "FROM movies "
                + "WHERE (title=? OR ? IS NULL)"
                + "AND (year=? OR ? IS NULL);";

        return executeQueryStatement(
                query,
                Movie::fromRS,
                title, title,
                year, year
        );
    }

    public Movie getMovie(String imdb) {
        String query = "SELECT * "
                + "FROM movies "
                + "WHERE imdb=?";

        List<Movie> movies = executeQueryStatement(query, Movie::fromRS, imdb);

        if (movies.size() > 0) {
            return movies.get(0);
        }

        return null;
    }

    public Performance insertPerformance(String imdb, String theater, String date, String time) {
        String sql = "INSERT "
                + "INTO performances(imdb, theatre_name, date, start_time) "
                + "VALUES (?, ?, ?, ?)";

        if (executeUpdateStatement(sql, imdb, theater, date, time) != 0) {
            String query = "SELECT * FROM performances WHERE rowid = last_insert_rowid();";
            List<Performance> performances = executeQueryStatement(query, Performance::fromRS);
            if (performances.size() > 0) {
                return performances.get(0);
            }
        }

        return null;
    }

    public List<Performance> getPerformances() {
        String query = "SELECT performance_id, date, start_time, title, year, theatre_name, (capacity - count(tickets.ticket_id)) AS seats_left "
                + "FROM performances "
                + "JOIN movies "
                + "USING (imdb) "
                + "JOIN theatres "
                + "USING (theatre_name) "
                + "LEFT OUTER JOIN tickets "
                + "USING (performance_id) "
                + "GROUP BY (performance_id);";

        return executeQueryStatement(query, Performance::fromRS);
    }

    public boolean authCustomer(String userId, String password) {
        String userQuery = "SELECT * FROM customers WHERE user_name=? AND password=?";
        List<Object> customers = executeQueryStatement(
                userQuery,
                (rs) -> null,
                userId,
                PasswordHashGenerator.hash(password)
        );
        return customers.size() == 1;
    }

    public boolean hasTicketsLeft(String performanceId) {
        String ticketCountSql = "SELECT count() FROM tickets WHERE performance_id=?";
        String capacitySql = "SELECT capacity "
                + "FROM performances "
                + "JOIN theatres "
                + "USING (theatre_name) "
                + "GROUP BY (performance_id) "
                + "HAVING performance_id=?;";


        Integer ticketCount = executeQueryStatement(ticketCountSql, (rs) -> rs.getInt(1), performanceId).get(0);
        Integer capacity = executeQueryStatement(capacitySql, (rs) -> rs.getInt(1), performanceId).get(0);

        return capacity > ticketCount;
    }

    public DBTicket insertTicket(String userId, String performanceId) {
        String sql = "INSERT "
                + "INTO tickets(user_name, performance_id) "
                + "VALUES (?, ?)";

        if (executeUpdateStatement(sql, userId, performanceId) != 0) {
            String query = "SELECT * FROM tickets WHERE rowid = last_insert_rowid();";
            List<DBTicket> tickets = executeQueryStatement(query, DBTicket::fromRS);
            if (tickets.size() > 0) {
                return tickets.get(0);
            }
        }

        return null;
    }

    public List<Ticket> getCustomerTickets(String userName) {
        String query = "SELECT ticket_id, date, start_time, theatre_name, title, year, count() AS ticket_count "
                + "FROM tickets "
                + "JOIN performances "
                + "USING (performance_id) "
                + "JOIN movies "
                + "USING (imdb) "
                + "WHERE user_name=? "
                + "GROUP BY (performance_id);";
        return executeQueryStatement(query, Ticket::fromRS, userName);
    }
}
