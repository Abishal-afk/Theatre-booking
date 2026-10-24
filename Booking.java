import java.util.List;

public class Booking {
    private String bookingId;
    private String customerName;
    private String customerPhone;
    private String showId;
    private List<String> seatNumbers;
    private double totalAmount;

    public Booking(String bookingId, String customerName, String customerPhone, 
                   String showId, List<String> seatNumbers, double totalAmount) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.showId = showId;
        this.seatNumbers = seatNumbers;
        this.totalAmount = totalAmount;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getShowId() {
        return showId;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String toCsv() {
        String seatsJoined = String.join(";", seatNumbers);
        return String.format("%s,%s,%s,%s,%s,%.2f",
                bookingId, customerName, customerPhone, showId, seatsJoined, totalAmount);
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Name: %s | Phone: %s | Show: %s | Seats: %s | Paid: ₹%.2f",
                bookingId, customerName, customerPhone, showId, seatNumbers, totalAmount);
    }
}
