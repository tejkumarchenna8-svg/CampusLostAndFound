package campuslostfound;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public final class Main {
    private static final Color NAVY = new Color(25, 39, 64);
    private static final Color BLUE = new Color(55, 111, 197);
    private static final Color BG = new Color(244, 247, 251);
    private static final Color MUTED = new Color(105, 119, 140);

    private final CardLayout pages = new CardLayout();
    private final JPanel pageHost = new JPanel(pages);
    private final JFrame frame = new JFrame("Campus Lost & Found");
    private final JLabel lostCount = new JLabel("—");
    private final JLabel foundCount = new JLabel("—");
    private final JLabel recoveredCount = new JLabel("—");
    private final JLabel status = new JLabel("  Ready");

    private Main() { }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new Main().show()); }

    private void show() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(900, 620));
        frame.setSize(1120, 740);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(BG);
        frame.add(sidebar(), BorderLayout.WEST);
        pageHost.setBackground(BG);
        pageHost.add(dashboard(), "dashboard");
        frame.add(pageHost, BorderLayout.CENTER);
        status.setFont(new Font("SansSerif", Font.PLAIN, 12));
        status.setForeground(MUTED);
        status.setBorder(new EmptyBorder(8, 18, 10, 18));
        frame.add(status, BorderLayout.SOUTH);
        frame.setVisible(true);
        refreshCounts();
    }

    private JPanel sidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setPreferredSize(new Dimension(245, 0));
        side.setBackground(NAVY);
        side.setBorder(new EmptyBorder(25, 18, 20, 18));
        JPanel upper = new JPanel();
        upper.setOpaque(false);
        upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("CAMPUS LOST");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 21));
        JLabel title2 = new JLabel("& FOUND");
        title2.setForeground(new Color(143, 187, 255));
        title2.setFont(new Font("SansSerif", Font.BOLD, 21));
        JLabel caption = new JLabel("STUDENT SERVICES");
        caption.setForeground(new Color(169, 185, 207));
        caption.setFont(new Font("SansSerif", Font.BOLD, 10));
        upper.add(title); upper.add(title2); upper.add(Box.createVerticalStrut(5)); upper.add(caption);
        upper.add(Box.createVerticalStrut(35));
        upper.add(navButton("Dashboard", () -> { pages.show(pageHost, "dashboard"); refreshCounts(); }));
        upper.add(navButton("Report Lost Item", () -> report(Item.Type.LOST)));
        upper.add(navButton("Report Found Item", () -> report(Item.Type.FOUND)));
        upper.add(navButton("Search Items", this::search));
        side.add(upper, BorderLayout.NORTH);
        JButton exit = navButton("Exit", frame::dispose);
        side.add(exit, BorderLayout.SOUTH);
        return side;
    }

    private JButton navButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(Color.WHITE);
        button.setBackground(NAVY);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(new EmptyBorder(12, 13, 12, 10));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(e -> action.run());
        return button;
    }

    private JPanel dashboard() {
        JPanel root = new JPanel(new BorderLayout(0, 24));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(38, 42, 30, 42));
        JPanel intro = new JPanel();
        intro.setOpaque(false);
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel("Welcome to your dashboard");
        heading.setForeground(NAVY);
        heading.setFont(new Font("SansSerif", Font.BOLD, 28));
        JLabel sub = new JLabel("Keep track of campus belongings and help reunite items with their owners.");
        sub.setForeground(MUTED);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        intro.add(heading); intro.add(Box.createVerticalStrut(8)); intro.add(sub);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);
        JPanel cards = new JPanel(new GridLayout(1, 3, 18, 18));
        cards.setOpaque(false);
        cards.add(summaryCard("TOTAL LOST ITEMS", lostCount, BLUE));
        cards.add(summaryCard("TOTAL FOUND ITEMS", foundCount, new Color(31, 139, 111)));
        cards.add(summaryCard("RECOVERED ITEMS", recoveredCount, new Color(120, 85, 180)));
        center.add(cards, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(1, 2, 18, 18));
        actions.setOpaque(false);
        actions.add(actionPanel("Report a lost item", "Create a report with its last known location.",
                "Report Lost Item", BLUE, () -> report(Item.Type.LOST)));
        actions.add(actionPanel("Report a found item", "Record an item you found on campus.",
                "Report Found Item", new Color(31, 139, 111), () -> report(Item.Type.FOUND)));
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(actions, BorderLayout.NORTH);
        JButton search = new JButton("Search saved reports");
        stylePrimary(search, new Color(120, 85, 180));
        search.addActionListener(e -> search());
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 20));
        searchRow.setOpaque(false); searchRow.add(search);
        bottom.add(searchRow, BorderLayout.CENTER);
        center.add(bottom, BorderLayout.CENTER);
        root.add(intro, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        return root;
    }

    private JPanel summaryCard(String label, JLabel value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 4, accent), new EmptyBorder(20, 20, 20, 18)));
        JLabel name = new JLabel(label);
        name.setForeground(MUTED);
        name.setFont(new Font("SansSerif", Font.BOLD, 11));
        value.setForeground(NAVY);
        value.setFont(new Font("SansSerif", Font.BOLD, 34));
        card.add(name, BorderLayout.NORTH); card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel actionPanel(String title, String description, String buttonText, Color color, Runnable action) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(22, 22, 22, 22));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel(title);
        heading.setForeground(NAVY); heading.setFont(new Font("SansSerif", Font.BOLD, 17));
        JLabel details = new JLabel(description);
        details.setForeground(MUTED); details.setFont(new Font("SansSerif", Font.PLAIN, 13));
        details.setBorder(new EmptyBorder(9, 0, 17, 0));
        JButton button = new JButton(buttonText);
        stylePrimary(button, color);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(e -> action.run());
        panel.add(heading); panel.add(details); panel.add(button);
        return panel;
    }

    private void stylePrimary(JButton button, Color color) {
        button.setBackground(color); button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setFocusPainted(false); button.setBorder(new EmptyBorder(11, 16, 11, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void report(Item.Type type) {
        if (type == Item.Type.LOST) {
            reportLostItem();
            return;
        }
        reportFoundItem();
    }

    private void reportLostItem() {
        JDialog dialog = new JDialog(frame, "Report Lost Item", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(0, 12));
        dialog.getContentPane().setBackground(BG);

        JPanel heading = new JPanel();
        heading.setBackground(Color.WHITE);
        heading.setBorder(new EmptyBorder(18, 22, 16, 22));
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Report a lost item");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel("Add the item and your contact details so it can be returned.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);
        dialog.add(heading, BorderLayout.NORTH);

        JTextField name = new JTextField(24);
        JComboBox<String> category = new JComboBox<>(new String[]{"Select a category", "ID Card", "Books", "Keys", "Wallet", "Electronics", "Other"});
        JTextArea description = new JTextArea(4, 24);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        JTextField location = new JTextField(24);
        JSpinner date = new JSpinner(new SpinnerDateModel());
        date.setEditor(new JSpinner.DateEditor(date, "yyyy-MM-dd"));
        JTextField studentName = new JTextField(24);
        JTextField contact = new JTextField(24);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setBorder(new EmptyBorder(18, 22, 18, 22));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        addFormField(fields, gbc, "Item Name *", name);
        gbc.gridy++;
        addFormField(fields, gbc, "Category *", category);
        gbc.gridy++;
        addFormField(fields, gbc, "Description *", new JScrollPane(description));
        gbc.gridy++;
        addFormField(fields, gbc, "Last Seen Location *", location);
        gbc.gridy++;
        addFormField(fields, gbc, "Date Lost *", date);
        gbc.gridy++;
        addFormField(fields, gbc, "Student Name *", studentName);
        gbc.gridy++;
        addFormField(fields, gbc, "Contact Number *", contact);
        dialog.add(new JScrollPane(fields), BorderLayout.CENTER);

        JButton submit = new JButton("Submit Lost Item");
        stylePrimary(submit, BLUE);
        JButton clear = new JButton("Clear Form");
        JButton cancel = new JButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttons.setBackground(BG);
        buttons.setBorder(new EmptyBorder(0, 14, 4, 14));
        buttons.add(clear);
        buttons.add(cancel);
        buttons.add(submit);
        dialog.add(buttons, BorderLayout.SOUTH);

        clear.addActionListener(e -> {
            name.setText("");
            category.setSelectedIndex(0);
            description.setText("");
            location.setText("");
            date.setValue(new java.util.Date());
            studentName.setText("");
            contact.setText("");
            name.requestFocusInWindow();
        });
        cancel.addActionListener(e -> dialog.dispose());
        submit.addActionListener(e -> {
            String itemName = name.getText().trim();
            String selectedCategory = category.getSelectedIndex() > 0 ? String.valueOf(category.getSelectedItem()) : "";
            String itemDescription = description.getText().trim();
            String itemLocation = location.getText().trim();
            String reporter = studentName.getText().trim();
            String phone = contact.getText().trim();
            if (itemName.isEmpty() || selectedCategory.isEmpty() || itemDescription.isEmpty()
                    || itemLocation.isEmpty() || reporter.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Complete all required fields before submitting.",
                        "Required fields", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String digits = phone.replaceAll("\\D", "");
            if (!phone.matches("\\+?[0-9][0-9 ()-]*") || digits.length() < 7 || digits.length() > 15) {
                JOptionPane.showMessageDialog(dialog, "Enter a valid contact number with 7 to 15 digits.",
                        "Invalid contact number", JOptionPane.WARNING_MESSAGE);
                contact.requestFocusInWindow();
                return;
            }
            String lostDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) date.getValue());
            LostItem item = new LostItem(itemName, selectedCategory, itemLocation, lostDate,
                    itemDescription, reporter, phone);
            try {
                ItemManager.getInstance().addItem(item);
                refreshCounts();
                status.setText("  Lost item report saved  |  Reference " + item.getId());
                dialog.dispose();
                JOptionPane.showMessageDialog(frame, "Lost item report saved successfully.\nReference: " + item.getId(),
                        "Report saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (ItemManager.StorageException ex) {
                showError(ex);
            }
        });
        dialog.getRootPane().setDefaultButton(submit);
        dialog.setMinimumSize(new Dimension(540, 660));
        dialog.setSize(600, 720);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String label, JComponent input) {
        JPanel row = new JPanel(new BorderLayout(0, 5));
        row.setOpaque(false);
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        fieldLabel.setForeground(NAVY);
        row.add(fieldLabel, BorderLayout.NORTH);
        row.add(input, BorderLayout.CENTER);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(row, gbc);
    }

    private void reportFoundItem() {
        JDialog dialog = new JDialog(frame, "Report Found Item", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(0, 12));
        dialog.getContentPane().setBackground(BG);

        JPanel heading = new JPanel();
        heading.setBackground(Color.WHITE);
        heading.setBorder(new EmptyBorder(18, 22, 16, 22));
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Report a found item");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel("Record the item and your contact details to help its owner find you.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);
        dialog.add(heading, BorderLayout.NORTH);

        JTextField name = new JTextField(24);
        JComboBox<String> category = new JComboBox<>(new String[]{"Select a category", "ID Card", "Books", "Keys", "Wallet", "Electronics", "Other"});
        JTextArea description = new JTextArea(4, 24);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        JTextField location = new JTextField(24);
        JSpinner date = new JSpinner(new SpinnerDateModel());
        date.setEditor(new JSpinner.DateEditor(date, "yyyy-MM-dd"));
        JTextField finderName = new JTextField(24);
        JTextField contact = new JTextField(24);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setBorder(new EmptyBorder(18, 22, 18, 22));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        addFormField(fields, gbc, "Item Name *", name);
        gbc.gridy++;
        addFormField(fields, gbc, "Category *", category);
        gbc.gridy++;
        addFormField(fields, gbc, "Description *", new JScrollPane(description));
        gbc.gridy++;
        addFormField(fields, gbc, "Found Location *", location);
        gbc.gridy++;
        addFormField(fields, gbc, "Date Found *", date);
        gbc.gridy++;
        addFormField(fields, gbc, "Finder Name *", finderName);
        gbc.gridy++;
        addFormField(fields, gbc, "Contact Number *", contact);
        dialog.add(new JScrollPane(fields), BorderLayout.CENTER);

        JButton submit = new JButton("Submit Found Item");
        stylePrimary(submit, new Color(31, 139, 111));
        JButton clear = new JButton("Clear Form");
        JButton cancel = new JButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttons.setBackground(BG);
        buttons.setBorder(new EmptyBorder(0, 14, 4, 14));
        buttons.add(clear);
        buttons.add(cancel);
        buttons.add(submit);
        dialog.add(buttons, BorderLayout.SOUTH);

        clear.addActionListener(e -> {
            name.setText("");
            category.setSelectedIndex(0);
            description.setText("");
            location.setText("");
            date.setValue(new java.util.Date());
            finderName.setText("");
            contact.setText("");
            name.requestFocusInWindow();
        });
        cancel.addActionListener(e -> dialog.dispose());
        submit.addActionListener(e -> {
            String itemName = name.getText().trim();
            String selectedCategory = category.getSelectedIndex() > 0 ? String.valueOf(category.getSelectedItem()) : "";
            String itemDescription = description.getText().trim();
            String itemLocation = location.getText().trim();
            String finder = finderName.getText().trim();
            String phone = contact.getText().trim();
            if (itemName.isEmpty() || selectedCategory.isEmpty() || itemDescription.isEmpty()
                    || itemLocation.isEmpty() || finder.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Complete all required fields before submitting.",
                        "Required fields", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String digits = phone.replaceAll("\\D", "");
            if (!phone.matches("\\+?[0-9][0-9 ()-]*") || digits.length() < 7 || digits.length() > 15) {
                JOptionPane.showMessageDialog(dialog, "Enter a valid contact number with 7 to 15 digits.",
                        "Invalid contact number", JOptionPane.WARNING_MESSAGE);
                contact.requestFocusInWindow();
                return;
            }
            String foundDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) date.getValue());
            FoundItem item = new FoundItem(itemName, selectedCategory, itemLocation, foundDate,
                    itemDescription, finder, phone);
            try {
                ItemManager.getInstance().addItem(item);
                refreshCounts();
                status.setText("  Found item report saved  |  Reference " + item.getId());
                dialog.dispose();
                JOptionPane.showMessageDialog(frame, "Found item report saved successfully.\nReference: " + item.getId(),
                        "Report saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (ItemManager.StorageException ex) {
                showError(ex);
            }
        });
        dialog.getRootPane().setDefaultButton(submit);
        dialog.setMinimumSize(new Dimension(540, 660));
        dialog.setSize(600, 720);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }
    private void search() {
        JDialog dialog = new JDialog(frame, "Search and Filter Items", Dialog.ModalityType.MODELESS);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(0, 12));
        dialog.getContentPane().setBackground(BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(18, 22, 18, 22));
        JLabel title = new JLabel("Search and filter reports");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel("Browse saved campus reports and update a found item's recovery status.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.add(title);
        titleStack.add(Box.createVerticalStrut(5));
        titleStack.add(subtitle);
        header.add(titleStack, BorderLayout.WEST);
        dialog.add(header, BorderLayout.NORTH);

        JTextField nameSearch = new JTextField(18);
        JComboBox<String> categoryFilter = new JComboBox<>(new String[]{"All Categories"});
        JComboBox<String> typeFilter = new JComboBox<>(new String[]{"All types", "Lost", "Found"});
        JComboBox<String> recoveryFilter = new JComboBox<>(new String[]{"All statuses", "Recovered", "Not Recovered"});
        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);
        filters.setBorder(new EmptyBorder(4, 14, 4, 14));
        addFilter(filters, 0, "Search item name", nameSearch);
        addFilter(filters, 1, "Category", categoryFilter);
        addFilter(filters, 2, "Type", typeFilter);
        addFilter(filters, 3, "Recovery status", recoveryFilter);
        JButton clearFilters = new JButton("Clear Filters");
        JButton refreshRecords = new JButton("Refresh Records");
        filters.add(clearFilters, filterConstraints(4));
        filters.add(refreshRecords, filterConstraints(5));

        String[] columns = {"Item ID", "Item Name", "Category", "Type", "Description", "Location", "Date", "Student/Finder Name", "Contact", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(27);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        int[] widths = {95, 150, 115, 70, 230, 150, 100, 160, 125, 115};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(model);
        table.setRowSorter(sorter);
        JScrollPane tableScroll = new JScrollPane(table);
        JLabel resultCount = new JLabel("  Loading records…");
        resultCount.setForeground(MUTED);
        resultCount.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(0, 14, 0, 14));
        center.add(filters, BorderLayout.NORTH);
        center.add(tableScroll, BorderLayout.CENTER);
        center.add(resultCount, BorderLayout.SOUTH);
        dialog.add(center, BorderLayout.CENTER);

        JButton recover = new JButton("Mark Selected as Recovered");
        stylePrimary(recover, new Color(120, 85, 180));
        recover.setEnabled(false);
        JButton close = new JButton("Close");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(0, 14, 2, 14));
        actions.add(close);
        actions.add(recover);
        dialog.add(actions, BorderLayout.SOUTH);

        Runnable applyFilters = () -> {
            java.util.List<javax.swing.RowFilter<DefaultTableModel, Integer>> active = new java.util.ArrayList<>();
            String text = nameSearch.getText().trim();
            if (!text.isEmpty()) active.add(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1));
            if (categoryFilter.getSelectedIndex() > 0) active.add(javax.swing.RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(String.valueOf(categoryFilter.getSelectedItem())) + "$", 2));
            if (typeFilter.getSelectedIndex() > 0) active.add(javax.swing.RowFilter.regexFilter("^" + String.valueOf(typeFilter.getSelectedItem()) + "$", 3));
            if (recoveryFilter.getSelectedIndex() > 0) active.add(javax.swing.RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(String.valueOf(recoveryFilter.getSelectedItem())) + "$", 9));
            sorter.setRowFilter(active.isEmpty() ? null : javax.swing.RowFilter.andFilter(active));
            resultCount.setText("  Showing " + table.getRowCount() + " of " + model.getRowCount() + " saved reports");
        };
        nameSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters.run(); }
        });
        categoryFilter.addActionListener(e -> applyFilters.run());
        typeFilter.addActionListener(e -> applyFilters.run());
        recoveryFilter.addActionListener(e -> applyFilters.run());
        clearFilters.addActionListener(e -> {
            nameSearch.setText("");
            categoryFilter.setSelectedIndex(0);
            typeFilter.setSelectedIndex(0);
            recoveryFilter.setSelectedIndex(0);
            table.clearSelection();
            applyFilters.run();
        });
        refreshRecords.addActionListener(e -> {
            try {
                ItemManager.LoadResult loaded = reloadSearchData(categoryFilter, model);
                applyFilters.run();
                showDamagedRecordWarning(dialog, loaded.getSkippedRecordCount());
            } catch (ItemManager.StorageException ex) {
                showError(ex);
            }
        });        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = table.getSelectedRow();
            boolean canRecover = false;
            if (viewRow >= 0) {
                int row = table.convertRowIndexToModel(viewRow);
                canRecover = "Found".equals(model.getValueAt(row, 3))
                        && "Not Recovered".equals(model.getValueAt(row, 9));
            }
            recover.setEnabled(canRecover);
        });
        recover.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(dialog, "Select a found item first.", "No item selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = table.convertRowIndexToModel(viewRow);
            String itemType = String.valueOf(model.getValueAt(row, 3));
            String itemStatus = String.valueOf(model.getValueAt(row, 9));
            if (!"Found".equals(itemType) || !"Not Recovered".equals(itemStatus)) {
                JOptionPane.showMessageDialog(dialog, "Only unrecovered found items can be marked recovered.", "Invalid selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = String.valueOf(model.getValueAt(row, 0));
            try {
                ItemManager.getInstance().markRecovered(id);
                ItemManager.LoadResult loaded = reloadSearchData(categoryFilter, model);
                applyFilters.run();
                table.clearSelection();
                refreshCounts(loaded.getItems());
                status.setText("  Recovery status updated for item " + id);
                showDamagedRecordWarning(dialog, loaded.getSkippedRecordCount());
                JOptionPane.showMessageDialog(dialog, "Item marked as recovered.", "Status updated", JOptionPane.INFORMATION_MESSAGE);
            } catch (ItemManager.StorageException ex) {
                showError(ex);
            }
        });
        close.addActionListener(e -> dialog.dispose());
        ItemManager.LoadResult initialLoad;
        try {
            initialLoad = reloadSearchData(categoryFilter, model);
            applyFilters.run();
        } catch (ItemManager.StorageException ex) {
            showError(ex);
            dialog.dispose();
            return;
        }
        dialog.setMinimumSize(new Dimension(1050, 520));
        dialog.setSize(1280, 650);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
        showDamagedRecordWarning(dialog, initialLoad.getSkippedRecordCount());
    }
    private GridBagConstraints filterConstraints(int column) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = column;
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = column == 0 ? 1 : 0;
        return gbc;
    }

    private void addFilter(JPanel panel, int column, String label, JComponent component) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = column;
        labelGbc.gridy = 0;
        labelGbc.anchor = GridBagConstraints.WEST;
        labelGbc.insets = new Insets(2, 5, 3, 5);
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setForeground(NAVY);
        fieldLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        panel.add(fieldLabel, labelGbc);
        GridBagConstraints inputGbc = filterConstraints(column);
        inputGbc.weightx = column == 0 ? 1 : 0;
        panel.add(component, inputGbc);
    }

    private ItemManager.LoadResult reloadSearchData(JComboBox<String> categoryFilter, DefaultTableModel model)
            throws ItemManager.StorageException {
        ItemManager.LoadResult loaded = ItemManager.getInstance().loadItems();
        List<Item> items = loaded.getItems();
        loadCategoryOptions(categoryFilter, items);
        loadSearchRows(model, items);
        return loaded;
    }

    private void loadCategoryOptions(JComboBox<String> categoryFilter, List<Item> items) {
        String selectedCategory = String.valueOf(categoryFilter.getSelectedItem());
        String[] presets = {"ID Card", "Books", "Keys", "Wallet", "Electronics", "Other"};
        java.util.List<String> options = new java.util.ArrayList<>(java.util.Arrays.asList(presets));
        java.util.Set<String> presetSet = new java.util.HashSet<>(options);
        java.util.Set<String> savedCategories = new java.util.TreeSet<>();
        for (Item item : items) {
            String category = item.getCategory().trim();
            if (!category.isEmpty() && !presetSet.contains(category)) savedCategories.add(category);
        }
        options.addAll(savedCategories);
        categoryFilter.removeAllItems();
        categoryFilter.addItem("All Categories");
        for (String category : options) categoryFilter.addItem(category);
        if (options.contains(selectedCategory)) categoryFilter.setSelectedItem(selectedCategory);
        else categoryFilter.setSelectedIndex(0);
    }

    private void loadSearchRows(DefaultTableModel model, List<Item> items) {
        model.setRowCount(0);
        for (Item item : items) {
            String type = item instanceof FoundItem ? "Found" : "Lost";
            String statusText = item.getType() == Item.Type.RECOVERED ? "Recovered" : "Not Recovered";
            model.addRow(new Object[]{item.getId(), item.getName(), item.getCategory(), type, item.getDescription(),
                    item.getLocation(), item.getDate(), item.getStudentName(), item.getContactNumber(), statusText});
        }
    }
    private void refreshCounts() {
        try {
            ItemManager.LoadResult loaded = ItemManager.getInstance().loadItems();
            refreshCounts(loaded.getItems());
            showDamagedRecordWarning(frame, loaded.getSkippedRecordCount());
        } catch (ItemManager.StorageException ex) {
            lostCount.setText("!");
            foundCount.setText("!");
            recoveredCount.setText("!");
            showError(ex);
        }
    }

    private void refreshCounts(List<Item> items) {
        int lost = 0, found = 0, recovered = 0;
        for (Item item : items) {
            if (item.getType() == Item.Type.LOST) lost++;
            else if (item.getType() == Item.Type.FOUND) found++;
            else recovered++;
        }
        lostCount.setText(String.valueOf(lost));
        foundCount.setText(String.valueOf(found));
        recoveredCount.setText(String.valueOf(recovered));
        status.setText("  Statistics loaded from data/items.txt");
    }

    private void showDamagedRecordWarning(Component parent, int skippedRecordCount) {
        if (skippedRecordCount <= 0) return;
        String message = "Some records could not be loaded. " + skippedRecordCount
                + " damaged record(s) were skipped. The original data file has not been changed.";
        JOptionPane.showMessageDialog(parent, message, "Some records could not be loaded", JOptionPane.WARNING_MESSAGE);
    }
    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(frame, "Could not access saved records.\n" + ex.getMessage(), "Storage error", JOptionPane.ERROR_MESSAGE);
    }
}