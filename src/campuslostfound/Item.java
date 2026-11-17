package campuslostfound;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/** Shared data and behavior for campus item reports. */
public abstract class Item {
    public enum Type { LOST, FOUND, RECOVERED }
    private final String id, name, category, location, date, description, studentName, contactNumber;

    protected Item(String id, String name, String category, String location, String date, String description) {
        this(id, name, category, location, date, description, "", "");
    }
    protected Item(String id, String name, String category, String location, String date, String description,
                   String studentName, String contactNumber) {
        this.id = id == null || id.trim().isEmpty() ? UUID.randomUUID().toString().toUpperCase() : id;
        this.name = clean(name); this.category = clean(category); this.location = clean(location);
        this.date = clean(date); this.description = clean(description);
        this.studentName = clean(studentName); this.contactNumber = clean(contactNumber);
    }
    protected Item(String name, String category, String location, String date, String description) {
        this(null, name, category, location, date, description);
    }
    private static String clean(String value) { return value == null ? "" : value.trim(); }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
    public String getStudentName() { return studentName; }
    public String getContactNumber() { return contactNumber; }
    public abstract Type getType();
    public abstract String getStatusLabel();
    public String toDisplayString() { return "[" + getStatusLabel() + "] " + name + " (#" + id + ")\nCategory: " + category + " | Location: " + location + " | Date: " + date + (description.isEmpty() ? "" : "\n" + description); }

    String toRecord() {
        return String.join("\t", getType().name(), encode(id), encode(name), encode(category), encode(location),
                encode(date), encode(description), encode(studentName), encode(contactNumber));
    }
    static Item fromRecord(String record) {
        String[] p = record.split("\t", -1);
        if (p.length != 7 && p.length != 9) throw new IllegalArgumentException("Invalid item record");
        Type t = Type.valueOf(p[0]);
        String id = decode(p[1]), name = decode(p[2]), category = decode(p[3]), location = decode(p[4]), date = decode(p[5]), description = decode(p[6]);
        String student = p.length == 9 ? decode(p[7]) : "";
        String contact = p.length == 9 ? decode(p[8]) : "";
        if (t == Type.LOST) return new LostItem(id, name, category, location, date, description, student, contact);
        if (t == Type.RECOVERED) return new FoundItem(id, name, category, location, date, description, student, contact, true);
        return new FoundItem(id, name, category, location, date, description, student, contact);
    }
    private static String encode(String s) { return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8)); }
    private static String decode(String s) { return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8); }
}