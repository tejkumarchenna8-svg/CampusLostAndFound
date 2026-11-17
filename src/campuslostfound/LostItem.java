package campuslostfound;

/** A report describing an item that has been misplaced. */
public final class LostItem extends Item {
    public LostItem(String name, String category, String location, String date, String description) {
        super(name, category, location, date, description);
    }
    public LostItem(String name, String category, String location, String date, String description,
                    String studentName, String contactNumber) {
        super(null, name, category, location, date, description, studentName, contactNumber);
    }
    LostItem(String id, String name, String category, String location, String date, String description) {
        super(id, name, category, location, date, description);
    }
    LostItem(String id, String name, String category, String location, String date, String description,
             String studentName, String contactNumber) {
        super(id, name, category, location, date, description, studentName, contactNumber);
    }
    @Override public Type getType() { return Type.LOST; }
    @Override public String getStatusLabel() { return "LOST"; }
}