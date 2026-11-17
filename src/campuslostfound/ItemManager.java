package campuslostfound;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

interface ItemRepository {
    void addItem(Item item) throws ItemManager.StorageException;
    List<Item> getAllItems() throws ItemManager.StorageException;
    List<Item> search(String query, Item.Type type) throws ItemManager.StorageException;
}

/** File-backed item management using the existing data/items.txt format. */
public final class ItemManager implements ItemRepository {
    private static final ItemManager INSTANCE = new ItemManager(Paths.get("data", "items.txt"));
    private final Path storageFile;

    ItemManager(Path storageFile) { this.storageFile = storageFile; }
    public static ItemManager getInstance() { return INSTANCE; }

    @Override public synchronized void addItem(Item item) throws StorageException {
        if (item == null) throw new IllegalArgumentException("Item cannot be null");
        List<Item> current = getAllItems();
        for (Item saved : current) {
            if (saved.getId().equals(item.getId())) {
                throw new StorageException("A report with ID " + item.getId() + " already exists.", null);
            }
        }
        try {
            Path parent = storageFile.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            byte[] record = (item.toRecord() + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
            if (Files.exists(storageFile) && Files.size(storageFile) > 0) {
                byte[] tail = new byte[1];
                try (java.io.InputStream input = Files.newInputStream(storageFile)) {
                    long skipped = Files.size(storageFile) - 1;
                    while (skipped > 0) skipped -= input.skip(skipped);
                    if (input.read(tail) == 1 && tail[0] != '\n' && tail[0] != '\r') {
                        byte[] prefixed = new byte[record.length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length];
                        byte[] separator = System.lineSeparator().getBytes(StandardCharsets.UTF_8);
                        System.arraycopy(separator, 0, prefixed, 0, separator.length);
                        System.arraycopy(record, 0, prefixed, separator.length, record.length);
                        record = prefixed;
                    }
                }
            }
            Files.write(storageFile, record, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            throw new StorageException("Unable to save a report.", ex);
        }
    }

    /** Replaces only the selected found record and keeps every other file line intact. */
    public synchronized void markRecovered(String id) throws StorageException {
        if (id == null || id.trim().isEmpty()) throw new StorageException("A valid item ID is required.", null);
        if (!Files.exists(storageFile)) throw new StorageException("The item record was not found.", null);
        Path tempFile = null;
        try {
            List<String> lines = Files.readAllLines(storageFile, StandardCharsets.UTF_8);
            boolean changed = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().isEmpty()) continue;
                try {
                    Item item = Item.fromRecord(line);
                    if (!item.getId().equals(id)) continue;
                    if (item.getType() != Item.Type.FOUND) {
                        throw new StorageException("The selected report is not an unrecovered found item.", null);
                    }
                    lines.set(i, ((FoundItem) item).asRecovered().toRecord());
                    changed = true;
                    break;
                } catch (IllegalArgumentException ex) {
                    // Keep malformed rows untouched; a damaged unrelated record must not block this update.
                }
            }
            if (!changed) throw new StorageException("The found item was not found or is already recovered.", null);
            Path parent = storageFile.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            tempFile = Files.createTempFile(parent, "items-", ".tmp");
            Files.write(tempFile, lines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(tempFile, storageFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tempFile, storageFile, StandardCopyOption.REPLACE_EXISTING);
            }
            tempFile = null;
        } catch (StorageException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new StorageException("Unable to update the report status.", ex);
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); }
                catch (IOException ex) { System.err.println("Could not remove temporary storage file: " + ex.getMessage()); }
            }
        }
    }

    /** Loads valid records and reports how many malformed rows were skipped. */
    public synchronized LoadResult loadItems() throws StorageException {
        List<Item> items = new ArrayList<>();
        int skippedRecords = 0;
        if (!Files.exists(storageFile)) return new LoadResult(items, skippedRecords);
        try {
            List<String> lines = Files.readAllLines(storageFile, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().isEmpty()) continue;
                try {
                    items.add(Item.fromRecord(line));
                } catch (RuntimeException ex) {
                    skippedRecords++;
                    System.err.println("Skipping damaged item record at line " + (i + 1) + " in " + storageFile + ".");
                }
            }
            return new LoadResult(items, skippedRecords);
        } catch (IOException ex) {
            throw new StorageException("Unable to read saved reports.", ex);
        }
    }

    @Override public synchronized List<Item> getAllItems() throws StorageException {
        return loadItems().getItems();
    }
    @Override public synchronized List<Item> search(String query, Item.Type type) throws StorageException {
        String term = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<Item> results = new ArrayList<>();
        for (Item item : getAllItems()) {
            if (type != null && item.getType() != type) continue;
            String text = (item.getName() + " " + item.getCategory() + " " + item.getLocation() + " "
                    + item.getDate() + " " + item.getDescription() + " " + item.getId()).toLowerCase(Locale.ROOT);
            if (term.isEmpty() || text.contains(term)) results.add(item);
        }
        return results;
    }

    public static final class LoadResult {
        private final List<Item> items;
        private final int skippedRecordCount;

        private LoadResult(List<Item> items, int skippedRecordCount) {
            this.items = new ArrayList<>(items);
            this.skippedRecordCount = skippedRecordCount;
        }

        public List<Item> getItems() { return new ArrayList<>(items); }
        public int getSkippedRecordCount() { return skippedRecordCount; }
    }
    public static final class StorageException extends Exception {
        public StorageException(String message, Throwable cause) { super(message, cause); }
    }
}