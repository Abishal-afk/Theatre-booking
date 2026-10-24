import java.io.IOException;
import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Map<String, Show> shows = new LinkedHashMap<>();
    private static final List<Booking> bookings = new ArrayList<>();

    public static void main(String[] args) {
        initializeSampleData();
        loadExistingBookings();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readIntInput("Select an option (1-6): ");

            switch (choice) {
                case 1 -> viewAvailableShows();
                case 2 -> viewSeatLayout();
                case 3 -> bookTickets();
                case 4 -> cancelBooking();
                case 5 -> viewAllBookings();
                case 6 -> {
                    System.out.println("\nThank you for using Theatre Management System. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid selection. Please choose between 1 and 6.");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n==============================================");
        System.out.println("        THEATRE TICKET BOOKING SYSTEM         ");
        System.out.println("==============================================");
        System.out.println("1. View Current Shows");
        System.out.println("2. View Seating Layout");
        System.out.println("3. Book Tickets");
        System.out.println("4. Cancel a Booking");
        System.out.println("5. View All Confirmed Bookings (Admin)");
        System.out.println("6. Exit");
        System.out.println("==============================================");
    }

    private static void initializeSampleData() {
        Movie movie1 = new Movie("M101", "Inception", "Sci-Fi", 148, 250.00);
        Movie movie2 = new Movie("M102", "Oppenheimer", "Drama", 180, 300.00);

        // Show 1: 5 rows (A-E), 8 columns (1-8)
        shows.put("SH01", new Show("SH01", movie1, "14:00", 5, 8));
        // Show 2: 6 rows (A-F), 10 columns (1-10)
        shows.put("SH02", new Show("SH02", movie2, "18:30", 6, 10));
    }

    private static void loadExistingBookings() {
        try {
            List<Booking> saved = FileHandler.loadBookings();
            bookings.addAll(saved);

            // Re-apply booked seats into show memory grids
            for (Booking b : saved) {
                Show show = shows.get(b.getShowId());
                if (show != null) {
                    for (String seat : b.getSeatNumbers()) {
                        try {
                            show.bookSeat(seat);
                        } catch (InvalidSeatException | SeatAlreadyBookedException ignored) {
                            // Already registered from persistent file
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Unable to load stored bookings (" + e.getMessage() + ")");
        }
    }

    private static void viewAvailableShows() {
        System.out.println("\n--- Current Available Shows ---");
        for (Show s : shows.values()) {
            System.out.printf("Show ID: %-5s | Time: %-5s | Available Seats: %-3d | %s\n",
                    s.getShowId(), s.getShowTime(), s.getAvailableSeatCount(), s.getMovie());
        }
    }

    private static void viewSeatLayout() {
        Show show = selectShow();
        if (show != null) {
            System.out.printf("\nShowing layout for %s at %s\n", show.getMovie().getTitle(), show.getShowTime());
            show.displaySeatLayout();
        }
    }

    private static void bookTickets() {
        Show show = selectShow();
        if (show == null) return;

        show.displaySeatLayout();

        System.out.print("Enter Customer Name: ");
        String name = scanner.nextLine().trim();
        while (name.isEmpty()) {
            System.out.print("Name cannot be empty. Enter Customer Name: ");
            name = scanner.nextLine().trim();
        }

        System.out.print("Enter Contact Number: ");
        String phone = scanner.nextLine().trim();
        while (!phone.matches("\\d{10}")) {
            System.out.print("Invalid format. Please enter a 10-digit phone number: ");
            phone = scanner.nextLine().trim();
        }

        int seatCount = readIntInput("How many seats do you want to book?: ");
        if (seatCount <= 0 || seatCount > show.getAvailableSeatCount()) {
            System.out.println("Invalid count or exceeds available seats (" + show.getAvailableSeatCount() + ").");
            return;
        }

        System.out.println("Enter seat IDs separated by comma or space (e.g., A1, A2): ");
        String rawSeats = scanner.nextLine();
        String[] tokens = rawSeats.toUpperCase().split("[,\\s]+");

        List<String> chosenSeats = new ArrayList<>();
        for (String t : tokens) {
            if (!t.trim().isEmpty()) {
                chosenSeats.add(t.trim());
            }
        }

        if (chosenSeats.size() != seatCount) {
            System.out.println("Mismatch: You declared " + seatCount + " seats but entered " + chosenSeats.size() + " seat codes.");
            return;
        }

        try {
            // Reserve seats in show matrix
            show.bookSeats(chosenSeats);

            double totalAmount = show.calculateTotalAmount(chosenSeats.size());
            String bookingId = "BKG" + (1000 + bookings.size() + 1);

            Booking booking = new Booking(bookingId, name, phone, show.getShowId(), chosenSeats, totalAmount);
            bookings.add(booking);

            // Persist to file
            FileHandler.appendBooking(booking);

            System.out.println("\n✓ BOOKING SUCCESSFUL!");
            System.out.println("----------------------------------------------");
            System.out.println("Booking Reference : " + bookingId);
            System.out.println("Customer Name     : " + name);
            System.out.println("Movie             : " + show.getMovie().getTitle());
            System.out.println("Seats Reserved    : " + chosenSeats);
            System.out.printf("Total Payable     : Rs. %.2f\n", totalAmount);
            System.out.println("----------------------------------------------");

        } catch (InvalidSeatException | SeatAlreadyBookedException e) {
            System.err.println("\nBooking Failed: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("\nFile Error: Unable to save booking data (" + e.getMessage() + ")");
        }
    }

    private static void cancelBooking() {
        System.out.print("\nEnter Booking ID to cancel (e.g., BKG1001): ");
        String bookingId = scanner.nextLine().trim().toUpperCase();

        Booking target = null;
        for (Booking b : bookings) {
            if (b.getBookingId().equalsIgnoreCase(bookingId)) {
                target = b;
                break;
            }
        }

        if (target == null) {
            System.out.println("Error: No record found with Booking ID: " + bookingId);
            return;
        }

        Show show = shows.get(target.getShowId());
        if (show != null) {
            try {
                // Free up seats in 2D array
                for (String seat : target.getSeatNumbers()) {
                    show.cancelSeat(seat);
                }

                // Remove from in-memory list and update file
                bookings.remove(target);
                FileHandler.rewriteBookings(bookings);

                System.out.println("✓ Booking " + bookingId + " has been successfully canceled and refunded.");
            } catch (InvalidSeatException e) {
                System.err.println("Cancellation Error: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("File Update Failed: " + e.getMessage());
            }
        }
    }

    private static void viewAllBookings() {
        System.out.println("\n--- Confirmed Bookings Audit ---");
        if (bookings.isEmpty()) {
            System.out.println("No active bookings on record.");
            return;
        }

        double totalRevenue = 0;
        for (Booking b : bookings) {
            System.out.println(b);
            totalRevenue += b.getTotalAmount();
        }
        System.out.printf("\nTotal Revenue Generated: Rs. %.2f\n", totalRevenue);
    }

    private static Show selectShow() {
        viewAvailableShows();
        System.out.print("\nEnter Show ID (e.g., SH01): ");
        String showId = scanner.nextLine().trim().toUpperCase();

        Show show = shows.get(showId);
        if (show == null) {
            System.out.println("Error: Invalid Show ID.");
            return null;
        }
        return show;
    }

    private static int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input. Please enter digits only.");
            }
        }
    }
}
