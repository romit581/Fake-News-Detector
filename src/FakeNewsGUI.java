import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class FakeNewsGUI extends JFrame {

    // ── Palette ───────────────────────────────────────────────────────────
    private static final Color BG        = new Color(15,  17,  26);
    private static final Color SURFACE   = new Color(24,  27,  41);
    private static final Color ACCENT    = new Color(99, 102, 241);   // indigo
    private static final Color FAKE_COL  = new Color(239,  68,  68);  // red
    private static final Color REAL_COL  = new Color(34,  197,  94);  // green
    private static final Color TEXT_PRI  = new Color(248, 250, 252);
    private static final Color TEXT_SEC  = new Color(148, 163, 184);
    private static final Color INPUT_BG  = new Color(30,  34,  54);
    private static final Color BORDER_C  = new Color(55,  65,  81);

    // ── Components ────────────────────────────────────────────────────────
    private final JTextArea  inputArea;
    private final JButton    checkBtn;
    private final JLabel     verdictLabel;
    private final JLabel     confidenceLabel;
    private final JLabel     reasonLabel;
    private final JLabel     spinnerLabel;
    private final JPanel     resultPanel;
    private       Timer      dotTimer;
    private       int        dotCount = 0;

    // ─────────────────────────────────────────────────────────────────────

    public FakeNewsGUI() {
        setTitle("AI Fake News Detector");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(640, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        // Lazy init fields for compiler
        inputArea       = findInputArea();
        checkBtn        = findCheckBtn();
        verdictLabel    = findVerdictLabel();
        confidenceLabel = findConfidenceLabel();
        reasonLabel     = findReasonLabel();
        spinnerLabel    = findSpinnerLabel();
        resultPanel     = findResultPanel();

        wireEvents();
        setVisible(true);
    }


    private JTextArea    _input;
    private JTextArea    _reasonArea;
    private JButton      _btn;
    private JLabel       _verdict;
    private JLabel       _confidence;
    private JLabel       _reason;
    private JLabel       _spinner;
    private JPanel       _resultPanel;

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(28, 32, 0, 32));

        JLabel title = new JLabel("AI News Detector");
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        title.setForeground(TEXT_PRI);

        JLabel sub = new JLabel("Powered by OpenRouter AI  •  Paste any headline or article below");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_SEC);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBackground(BG);
        text.add(title);
        text.add(Box.createVerticalStrut(4));
        text.add(sub);

        p.add(text, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCenter() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(BG);
        outer.setBorder(new EmptyBorder(20, 32, 0, 32));

        // Input area
        _input = new JTextArea(6, 40);
        _input.setLineWrap(true);
        _input.setWrapStyleWord(true);
        _input.setBackground(INPUT_BG);
        _input.setForeground(TEXT_PRI);
        _input.setCaretColor(ACCENT);
        _input.setFont(new Font("SansSerif", Font.PLAIN, 14));
        _input.setBorder(new EmptyBorder(10, 12, 10, 12));
        _input.setText(""); // placeholder via FocusListener below

        addPlaceholder(_input, "Paste your news headline or article here…");

        JScrollPane scroll = new JScrollPane(_input);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_C, 1));
        scroll.setBackground(INPUT_BG);
        scroll.getViewport().setBackground(INPUT_BG);
        outer.add(scroll);
        outer.add(Box.createVerticalStrut(14));

        // Button
        _btn = new JButton("Analyze with AI");
        _btn.setBackground(ACCENT);
        _btn.setForeground(Color.WHITE);
        _btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        _btn.setFocusPainted(false);
        _btn.setBorderPainted(false);
        _btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        _btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        _btn.setAlignmentX(LEFT_ALIGNMENT);
        outer.add(_btn);
        outer.add(Box.createVerticalStrut(20));

        // Spinner label (hidden by default)
        _spinner = new JLabel("Analyzing");
        _spinner.setFont(new Font("SansSerif", Font.ITALIC, 13));
        _spinner.setForeground(ACCENT);
        _spinner.setVisible(false);
        outer.add(_spinner);

        // Result card
        _resultPanel = new JPanel();
        _resultPanel.setLayout(new BoxLayout(_resultPanel, BoxLayout.Y_AXIS));
        _resultPanel.setBackground(SURFACE);
        _resultPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_C, 1),
                new EmptyBorder(16, 20, 16, 20)
        ));
        _resultPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        _resultPanel.setVisible(false);

        _verdict = new JLabel("REAL NEWS ✅");
        _verdict.setFont(new Font("Georgia", Font.BOLD, 22));
        _verdict.setForeground(REAL_COL);

        _confidence = new JLabel("Confidence: 85%");
        _confidence.setFont(new Font("SansSerif", Font.PLAIN, 13));
        _confidence.setForeground(TEXT_SEC);

        _reason = new JLabel("Reason:");
        _reason.setFont(new Font("SansSerif", Font.BOLD, 13));
        _reason.setForeground(TEXT_SEC);

        _reasonArea = new JTextArea(4, 40);
        _reasonArea.setLineWrap(true);
        _reasonArea.setWrapStyleWord(true);
        _reasonArea.setEditable(false);
        _reasonArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        _reasonArea.setForeground(TEXT_PRI);
        _reasonArea.setBackground(SURFACE);
        _reasonArea.setBorder(new EmptyBorder(4, 0, 4, 0));

        JScrollPane reasonScroll = new JScrollPane(_reasonArea);
        reasonScroll.setBorder(BorderFactory.createEmptyBorder());
        reasonScroll.setBackground(SURFACE);
        reasonScroll.getViewport().setBackground(SURFACE);
        reasonScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        _resultPanel.add(_verdict);
        _resultPanel.add(Box.createVerticalStrut(6));
        _resultPanel.add(_confidence);
        _resultPanel.add(Box.createVerticalStrut(8));
        _resultPanel.add(new JSeparator());
        _resultPanel.add(Box.createVerticalStrut(8));
        _resultPanel.add(_reason);
        _resultPanel.add(Box.createVerticalStrut(4));
        _resultPanel.add(reasonScroll);

        outer.add(_resultPanel);
        return outer;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(8, 0, 12, 0));
        JLabel note = new JLabel("Results are AI-generated and should not be taken as definitive fact");
        note.setFont(new Font("SansSerif", Font.ITALIC, 11));
        note.setForeground(new Color(75, 85, 99));
        p.add(note);
        return p;
    }


    private JTextArea findInputArea()       { return _input; }
    private JButton   findCheckBtn()        { return _btn; }
    private JLabel    findVerdictLabel()    { return _verdict; }
    private JLabel    findConfidenceLabel() { return _confidence; }
    private JLabel    findReasonLabel()     { return _reason; }
    private JLabel    findSpinnerLabel()    { return _spinner; }
    private JPanel    findResultPanel()     { return _resultPanel; }

    // ── Wiring ────────────────────────────────────────────────────────────

    private void wireEvents() {
        _btn.addActionListener(e -> runDetection());
        _input.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) runDetection();
            }
        });
    }

    private void runDetection() {
        String news = _input.getText().strip();
        if (news.isEmpty() || news.equals("Paste your news headline or article here…")) {
            showError("Please enter a news headline or article to analyze.");
            return;
        }

        setLoading(true);

        SwingWorker<FakeNewsDetector.DetectionResult, Void> worker = new SwingWorker<>() {
            @Override
            protected FakeNewsDetector.DetectionResult doInBackground() throws Exception {
                return FakeNewsDetector.detectNews(news);
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    showResult(get());
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    showError("Error: " + msg);
                }
            }
        };
        worker.execute();
    }

    // ── UI state ──────────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        _btn.setEnabled(!loading);
        _resultPanel.setVisible(false);

        if (loading) {
            _spinner.setVisible(true);
            dotCount = 0;
            dotTimer = new Timer(400, e -> {
                dotCount = (dotCount + 1) % 4;
                _spinner.setText("Analyzing" + ".".repeat(dotCount));
            });
            dotTimer.start();
        } else {
            if (dotTimer != null) dotTimer.stop();
            _spinner.setVisible(false);
        }
    }

    private void showResult(FakeNewsDetector.DetectionResult r) {
        boolean fake = r.isFake();

        _verdict.setText(fake ? "FAKE NEWS ❌" : "REAL NEWS ✅");
        _verdict.setForeground(fake ? FAKE_COL : REAL_COL);
        _confidence.setText("AI Confidence: " + r.confidence() + "%");
        _reasonArea.setText(r.reason());
        _reasonArea.setCaretPosition(0);

        _resultPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fake ? FAKE_COL : REAL_COL, 2),
                new EmptyBorder(16, 20, 16, 20)
        ));
        _resultPanel.setVisible(true);
        revalidate();
        repaint();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Placeholder helper ────────────────────────────────────────────────

    private static void addPlaceholder(JTextArea area, String placeholder) {
        Color normal      = new Color(248, 250, 252);
        Color placeholderC = new Color(100, 116, 139);

        area.setForeground(placeholderC);
        area.setText(placeholder);

        area.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (area.getText().equals(placeholder)) {
                    area.setText("");
                    area.setForeground(normal);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (area.getText().isBlank()) {
                    area.setForeground(placeholderC);
                    area.setText(placeholder);
                }
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(FakeNewsGUI::new);
    }
}