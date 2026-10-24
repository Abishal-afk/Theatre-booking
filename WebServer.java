import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WebServer {
    private static final int PORT = 8080;
    private static final Map<String, Show> shows = new LinkedHashMap<>();
    private static final List<Booking> bookings = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // 1. Initialize your existing Java OOP structures
        Movie movie1 = new Movie("M101", "Inception", "Sci-Fi", 148, 250.00);
        Movie movie2 = new Movie("M102", "Oppenheimer", "Drama", 180, 300.00);

        shows.put("SH01", new Show("SH01", movie1, "14:00", 5, 8));
        shows.put("SH02", new Show("SH02", movie2, "18:30", 6, 10));

        // Load records from bookings.txt using your FileHandler
        loadBookings();

        // 2. Start Java Built-in HTTP Server
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API Endpoint to check show info and booked seats
        server.createContext("/api/shows", new ShowsHandler());

        // API Endpoint to process ticket booking using Java OOP & File Handling
        server.createContext("/api/book", new BookingHandler());

        // Serves the index.html file to the browser
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        System.out.println("==================================================");
        System.out.println("Java Web Server running at: http://localhost:" + PORT);
        System.out.println("Your Java OOP backend is now powering the website!");
        System.out.println("==================================================");
        server.start();
    }

    private static void loadBookings() {
        try {
            List<Booking> saved = FileHandler.loadBookings();
            bookings.addAll(saved);
            for (Booking b : saved) {
                Show show = shows.get(b.getShowId());
                if (show != null) {
                    for (String seat : b.getSeatNumbers()) {
                        try {
                            show.bookSeat(seat);
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Notice: No previous bookings found.");
        }
    }

    // Handles GET /api/shows
    static class ShowsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

            StringBuilder json = new StringBuilder("[");
            int count = 0;
            for (Show s : shows.values()) {
                if (count++ > 0) json.append(",");
                json.append(String.format(
                    "{\"id\":\"%s\",\"title\":\"%s\",\"time\":\"%s\",\"price\":%.2f}",
                    s.getShowId(), s.getMovie().getTitle(), s.getShowTime(), s.getMovie().getBasePrice()
                ));
            }
            json.append("]");

            byte[] response = json.toString().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }

    // Handles POST /api/book
    static class BookingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "*");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                String body = br.readLine();

                // Simple JSON parser for: {"showId":"SH01","name":"John","phone":"9876543210","seats":["A1","A2"]}
                try {
                    String showId = extractJsonValue(body, "showId");
                    String name = extractJsonValue(body, "name");
                    String phone = extractJsonValue(body, "phone");
                    List<String> seats = extractJsonArray(body, "seats");

                    Show show = shows.get(showId);
                    if (show == null) throw new Exception("Invalid Show ID");

                    // Trigger your existing Java Exception Handling and Seating validation
                    show.bookSeats(seats);

                    double amount = show.calculateTotalAmount(seats.size());
                    String bId = "BKG" + (1000 + bookings.size() + 1);
                    Booking b = new Booking(bId, name, phone, showId, seats, amount);
                    bookings.add(b);

                    // Saves to your real bookings.txt file on your hard drive!
                    FileHandler.appendBooking(b);

                    String res = String.format("{\"success\":true,\"bookingId\":\"%s\",\"amount\":%.2f}", bId, amount);
                    sendResponse(exchange, 200, res);

                } catch (InvalidSeatException | SeatAlreadyBookedException e) {
                    // Returns custom Java exceptions back to browser
                    String err = String.format("{\"success\":false,\"error\":\"%s\"}", e.getMessage());
                    sendResponse(exchange, 400, err);
                } catch (Exception e) {
                    String err = String.format("{\"success\":false,\"error\":\"%s\"}", e.getMessage());
                    sendResponse(exchange, 500, err);
                }
            }
        }

        private void sendResponse(HttpExchange exchange, int code, String json) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String extractJsonValue(String json, String key) {
            String pattern = "\"" + key + "\":\"";
            int start = json.indexOf(pattern) + pattern.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        }

        private List<String> extractJsonArray(String json, String key) {
            String pattern = "\"" + key + "\":[";
            int start = json.indexOf(pattern) + pattern.length();
            int end = json.indexOf("]", start);
            String raw = json.substring(start, end).replace("\"", "").trim();
            return Arrays.asList(raw.split(","));
        }
    }

    // Serves the index.html file to your browser
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            File file = new File("index.html");
            if (!file.exists()) {
                String notFound = "index.html not found!";
                exchange.sendResponseHeaders(404, notFound.length());
                exchange.getResponseBody().write(notFound.getBytes());
                return;
            }

            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, file.length());
            try (OutputStream os = exchange.getResponseBody(); FileInputStream fs = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int count;
                while ((count = fs.read(buffer)) >= 0) {
                    os.write(buffer, 0, count);
                }
            }
        }
    }
}
