package campuslostfound;

/** A found report, optionally marked as recovered by its owner. */
public final class FoundItem extends Item {
    private final boolean recovered;
    public FoundItem(String name, String category, String location, String date, String description) {
        super(name, category, location, date, description);
        this.recovered = false;
    }
    public FoundItem(String name, String category, String location, String date, String description,
                     String finderName, String contactNumber) {
        super(null, name, category, location, date, description, finderName, contactNumber);
        this.recovered = false;
    }
    FoundItem(String id, String name, String category, String location, String date, String description) {
        super(id, name, category, location, date, description);
        this.recovered = false;
    }
    FoundItem(String id, String name, String category, String location, String date, String description,
              String finderName, String contactNumber) {
        super(id, name, category, location, date, description, finderName, contactNumber);
        this.recovered = false;
    }
    FoundItem(String id, String name, String category, String location, String date, String description,
              boolean recovered) {
        this(id, name, category, location, date, description, "", "", recovered);
    }
    FoundItem(String id, String name, String category, String location, String date, String description,
              String finderName, String contactNumber, boolean recovered) {
        super(id, name, category, location, date, description, finderName, contactNumber);
        this.recovered = recovered;
    }
    @Override public Type getType() { return recovered ? Type.RECOVERED : Type.FOUND; }
    @Override public String getStatusLabel() { return recovered ? "RECOVERED" : "FOUND"; }
    FoundItem asRecovered() {
        return new FoundItem(getId(), getName(), getCategory(), getLocation(), getDate(), getDescription(),
                getStudentName(), getContactNumber(), true);
    }
}