public class Movie {
    private String movieId;
    private String title;
    private String genre;
    private int durationMinutes;
    private double basePrice;

    public Movie(String movieId, String title, String genre, int durationMinutes, double basePrice) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
        this.basePrice = basePrice;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public String toCsv() {
        return movieId + "," + title + "," + genre + "," + durationMinutes + "," + basePrice;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Genre: %s | %d mins | Base Price: ₹%.2f",
                movieId, title, genre, durationMinutes, basePrice);
    }
}
