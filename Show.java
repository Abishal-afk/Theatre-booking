import java.util.ArrayList;
import java.util.List;

public class Show {
    private String showId;
    private Movie movie;
    private String showTime;
    private boolean[][] seats; // false = Available, true = Booked
    private int totalRows;
    private int totalCols;

    public Show(String showId, Movie movie, String showTime, int rows, int cols) {
        if (rows > 26) {
            throw new IllegalArgumentException("Maximum supported rows is 26 ('A' - 'Z').");
        }
        this.showId = showId;
        this.movie = movie;
        this.showTime = showTime;
        this.totalRows = rows;
        this.totalCols = cols;
        this.seats = new boolean[rows][cols];
    }

    public String getShowId() {
        return showId;
    }

    public Movie getMovie() {
        return movie;
    }

    public String getShowTime() {
        return showTime;
    }

    public void displaySeatLayout() {
        System.out.println("\n=================================================");
        System.out.println("            SCREEN THIS WAY (FRONT)              ");
        System.out.println("=================================================");
        
        System.out.print("    ");
        for (int c = 1; c <= totalCols; c++) {
            System.out.printf("%4d", c);
        }
        System.out.println("\n");

        for (int r = 0; r < totalRows; r++) {
            char rowChar = (char) ('A' + r);
            System.out.printf("%2c  ", rowChar);
            for (int c = 0; c < totalCols; c++) {
                if (seats[r][c]) {
                    System.out.print(" [X]");
                } else {
                    System.out.print(" [ ]");
                }
            }
            System.out.println();
        }
        System.out.println("\nLegend: [ ] Available | [X] Booked\n");
    }

    public void bookSeat(String seatCode) throws InvalidSeatException, SeatAlreadyBookedException {
        int[] coords = parseSeatCode(seatCode);
        int r = coords[0];
        int c = coords[1];

        if (seats[r][c]) {
            throw new SeatAlreadyBookedException("Seat " + seatCode.toUpperCase() + " is already booked!");
        }

        seats[r][c] = true;
    }

    public void bookSeats(List<String> seatCodes) throws InvalidSeatException, SeatAlreadyBookedException {
        List<int[]> validatedCoords = new ArrayList<>();

        for (String seatCode : seatCodes) {
            int[] coords = parseSeatCode(seatCode);
            int r = coords[0];
            int c = coords[1];

            if (seats[r][c]) {
                throw new SeatAlreadyBookedException("Seat " + seatCode.toUpperCase() + " is already taken.");
            }
            
            for (int[] existing : validatedCoords) {
                if (existing[0] == r && existing[1] == c) {
                    throw new InvalidSeatException("Duplicate seat specified in request: " + seatCode);
                }
            }
            validatedCoords.add(coords);
        }

        for (int[] coords : validatedCoords) {
            seats[coords[0]][coords[1]] = true;
        }
    }

    public void cancelSeat(String seatCode) throws InvalidSeatException {
        int[] coords = parseSeatCode(seatCode);
        int r = coords[0];
        int c = coords[1];

        if (!seats[r][c]) {
            throw new InvalidSeatException("Seat " + seatCode.toUpperCase() + " was not booked.");
        }

        seats[r][c] = false;
    }

    private int[] parseSeatCode(String seatCode) throws InvalidSeatException {
        if (seatCode == null || seatCode.trim().length() < 2) {
            throw new InvalidSeatException("Seat identifier '" + seatCode + "' is malformed.");
        }

        String cleaned = seatCode.trim().toUpperCase();
        char rowChar = cleaned.charAt(0);

        if (rowChar < 'A' || rowChar >= ('A' + totalRows)) {
            throw new InvalidSeatException("Row '" + rowChar + "' is out of range. Valid rows: A-" + (char) ('A' + totalRows - 1));
        }

        int col;
        try {
            col = Integer.parseInt(cleaned.substring(1));
        } catch (NumberFormatException e) {
            throw new InvalidSeatException("Invalid column number in '" + seatCode + "'.");
        }

        if (col < 1 || col > totalCols) {
            throw new InvalidSeatException("Column '" + col + "' is out of range. Valid columns: 1-" + totalCols);
        }

        return new int[]{rowChar - 'A', col - 1};
    }

    public int getAvailableSeatCount() {
        int count = 0;
        for (int r = 0; r < totalRows; r++) {
            for (int c = 0; c < totalCols; c++) {
                if (!seats[r][c]) {
                    count++;
                }
            }
        }
        return count;
    }

    public double calculateTotalAmount(int numberOfSeats) {
        return numberOfSeats * movie.getBasePrice();
    }
}
