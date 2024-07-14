package com.sdovan1;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.icons.FlatSearchIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class App extends JFrame {
    private JList<FontItem> fontList;
    private JTextField searchField;
    private JTextField previewText;
    private JSlider fontSizeSlider;
    private JLabel fontSizeLabel;
    private DefaultListModel<FontItem> listModel;
    private List<FontItem> allFonts;

    public App() {
        FlatLightLaf.setup();
        setTitle("Font Book");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Initialize components
        initializeComponents();

        // Set up the main layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Populate list
        populateFontList();
        setVisible(true);
    }

    private void initializeComponents() {
        listModel = new DefaultListModel<>();
        fontList = new JList<>(listModel);
        fontList.setCellRenderer(new FontItemRenderer());
        fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search Fonts");
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());

        previewText = new JTextField("The quick brown fox jumps over the lazy dog");
        previewText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));

        fontSizeSlider = new JSlider(JSlider.HORIZONTAL, 8, 72, 24);
        fontSizeLabel = new JLabel("24 pt");
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(searchField, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                filterFonts();
            }

            public void removeUpdate(DocumentEvent e) {
                filterFonts();
            }

            public void insertUpdate(DocumentEvent e) {
                filterFonts();
            }
        });

        return topPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(new JScrollPane(fontList), BorderLayout.CENTER);
        centerPanel.add(previewText, BorderLayout.SOUTH);

        previewText.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateList();
            }

            public void removeUpdate(DocumentEvent e) {
                updateList();
            }

            public void insertUpdate(DocumentEvent e) {
                updateList();
            }
        });

        return centerPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomPanel.add(new JLabel("Font Size:"));
        bottomPanel.add(fontSizeSlider);
        bottomPanel.add(fontSizeLabel);

        fontSizeSlider.addChangeListener(e -> {
            int fontSize = fontSizeSlider.getValue();
            fontSizeLabel.setText(fontSize + " pt");
            updateList();
        });

        return bottomPanel;
    }

    private void populateFontList() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        allFonts = Arrays.stream(ge.getAllFonts())
                .map(FontItem::new)
                .sorted(Comparator.comparing(fi -> fi.font.getFontName()))
                .collect(Collectors.toList());

        allFonts.forEach(listModel::addElement);
    }

    private void filterFonts() {
        String searchText = searchField.getText().toLowerCase();
        listModel.clear();
        fontList.setCellRenderer(new FontItemRenderer());
        allFonts.stream()
                .filter(fi -> fi.familyName.toLowerCase().contains(searchText) ||
                        fi.logicalName.toLowerCase().contains(searchText))
                .forEach(listModel::addElement);
    }

    private void updateList() {
        fontList.repaint();
    }

    private class FontItem {
        Font font;
        String familyName;
        String logicalName;

        FontItem(Font font) {
            this.font = font;
            this.familyName = font.getFontName();
            this.logicalName = font.getName();
        }
    }

    private class FontItemRenderer extends JPanel implements ListCellRenderer<FontItem> {
        private final JLabel nameLabel = new JLabel();
        private final JLabel previewLabel = new JLabel();
        /*
        A simple flag for loading fonts. `setFont` is time-consuming job. Thus, we could
        render default font in first call.
        */
        private final List<FontItem> loadedFonts = new ArrayList<>();

        FontItemRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(10, 10, 10, 10));
            add(nameLabel);

            // Monkey patch for increasing default preview size.
            previewLabel.setFont(previewLabel.getFont().deriveFont(24f));
            add(previewLabel);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends FontItem> list, FontItem value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            nameLabel.setText(value.familyName);
            previewLabel.setText(previewText.getText());

            if (!loadedFonts.contains(value)) {
                loadedFonts.add(value);
            } else {
                previewLabel.setFont(value.font.deriveFont((float) fontSizeSlider.getValue()));
                list.setCellRenderer(new FontItemRenderer());
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                nameLabel.setForeground(list.getSelectionForeground());
                previewLabel.setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                nameLabel.setForeground(list.getForeground());
                previewLabel.setForeground(list.getForeground());
            }

            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }
}
