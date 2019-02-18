import com.google.gson.Gson;
import com.google.gson.internal.bind.SqlDateTypeAdapter;
import spark.Spark;
import spark.servlet.SparkApplication;

import java.sql.Date;
import java.text.DateFormat;
import java.util.List;

import static spark.Spark.*;

public class Server {

    private final Database database = new Database();
    private final Gson gson = new Gson();

    public void run() {
        database.connect();

        Spark.port(7007);

        get("/ping", (req, res) -> "pong");
        post("/reset", (req, res) -> {
            database.resetDB();
            return "OK";
        });

        get("/movies", (req, res) -> {
            String title = req.queryParams("title");
            Integer year = null;
            try {
                year = Integer.parseInt(req.queryParams("year"));
            } catch (NumberFormatException e) {
                // We don't care about this
            }

            List<Movie> movies = database.getMovies(title, year);
            return "{ \"data\": " + gson.toJson(movies) + "}";
        });


        get("/movies/:imdb-key", (req, res) -> {
            String imdb = req.params(":imdb-key");

            Movie movie = database.getMovie(imdb);
            if (movie != null) {
                return "{ \"data\": [" + gson.toJson(movie) + "]}";
            } else {
                res.status(404);
                return "Not Found";
            }
        });

        post("/performances", (req, res) -> {
            String imdb = req.queryParams("imdb");
            String theater = req.queryParams("theater");
            String date = req.queryParams("date");
            String time = req.queryParams("time");

            Performance performance = database.insertPerformance(imdb, theater, date, time);
            if (performance != null) {
                return "/performances/" + performance.performanceId;
            }

            res.status(500);
            return "No such movie or theater";
        });

        get("/performances", (req, res) -> {
            List<Performance> performances = database.getPerformances();
            return "{ \"data\": " + gson.toJson(performances) + "}";
        });

        post("/tickets", (req, res) -> {
            String userId = req.queryParams("user");
            String performanceId = req.queryParams("performance");
            String password = req.queryParams("pwd");

            if (database.authCustomer(userId, password)) {
                if (database.hasTicketsLeft(performanceId)) {
                    DBTicket ticket = database.insertTicket(userId, performanceId);
                    if (ticket != null) {
                        return "/tickets/" + ticket.ticketId;
                    }
                } else {
                    return "No tickets left";
                }

                return "Error";
            } else {
                return "Wrong password";
            }
        });

        get("/customers/:userName/tickets", (req, res) -> {
            String userName = req.params(":userName");

            List<Ticket> tickets = database.getCustomerTickets(userName);
            return "{ \"data\": " + gson.toJson(tickets) + "}";
        });
    }

    public static void main(String[] args) {
        new Server().run();
    }
}
