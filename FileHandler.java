import java.io.*;
import java.util.*;

public class FileHandler {
    private static final String BOOKINGS_FILE = "bookings.txt";

    public static void appendBooking(Booking booking) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKINGS_FILE, true))) {
            bw.write(booking.toCsv());
            bw.newLine();
        }
    }

    public static List<Booking> loadBookings() throws IOException {
        List<Booking> list = new ArrayList<>();
        File file = new File(BOOKINGS_FILE);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String bId = parts[0];
                    String name = parts[1];
                    String phone = parts[2];
                    String showId = parts[3];
                    List<String> seats = Arrays.asList(parts[4].split(";"));
                    double amt = Double.parseDouble(parts[5]);
                    list.add(new Booking(bId, name, phone, showId, seats, amt));
                }
            }
        }
        return list;
    }

    public static void rewriteBookings(List<Booking> bookings) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKINGS_FILE, false))) {
            for (Booking b : bookings) {
                bw.write(b.toCsv());
                bw.newLine();
            }
        }
    }
}
