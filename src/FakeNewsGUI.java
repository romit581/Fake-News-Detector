import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * NewsDetectorAI — JavaFX GUI matching the Canva design.
 *
 * Fonts — place these in src/fonts/ folder:
 *   Horizon.otf
 *   Garet-Book.ttf
 *   LeagueSpartan-Bold.ttf
 *
 * Add JavaFX to your IntelliJ project:
 *   File > Project Structure > Libraries > + > Maven > org.openjfx:javafx-controls:21
 *
 * Environment variable required:
 *   OPENROUTER_API_KEY=your_key_here
 */
public class FakeNewsGUI extends Application {

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final String BG          = "#0d0f1a";
    private static final String CARD        = "#13162a";
    private static final String INPUT_BG    = "#1a1f36";
    private static final String BORDER      = "#2d3561";
    private static final String PURPLE      = "#7c3aed";
    private static final String PURPLE_HOV  = "#6d28d9";
    private static final String GREEN       = "#22c55e";
    private static final String RED         = "#ef4444";
    private static final String WHITE       = "#ffffff";
    private static final String MUTED       = "#6b7280";

    // ── Font handles ─────────────────────────────────────────────────────────
    private String horizonFamily      = "Arial Black";
    private String garetFamily        = "SansSerif";
    private String leagueFamily       = "SansSerif";

    // ── UI nodes ─────────────────────────────────────────────────────────────
    private TextArea inputArea;
    private Button   analyzeBtn;
    private Label    verdictLabel;
    private Label    confidenceLabel;
    private TextArea reasonArea;
    private VBox     resultBox;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void start(Stage stage) {
        loadFonts();

        stage.setTitle("News Detector AI");
        stage.setResizable(false);
        stage.setWidth(520);
        stage.setHeight(720);

        VBox root = buildRoot();
        Scene scene = new Scene(root, 520, 720, Color.web(BG));
        stage.setScene(scene);
        stage.show();

        // ── Entrance: drop down from above ───────────────────────────────
        root.setTranslateY(-50);
        root.setOpacity(0);
        Timeline drop = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(root.translateYProperty(), -50),
                        new KeyValue(root.opacityProperty(), 0)
                ),
                new KeyFrame(Duration.millis(550),
                        new KeyValue(root.translateYProperty(), 0,  Interpolator.EASE_OUT),
                        new KeyValue(root.opacityProperty(),    1,  Interpolator.EASE_OUT)
                )
        );
        drop.play();
    }

    // ── Font loader ───────────────────────────────────────────────────────────
    private void loadFonts() {
        try {
            Font h = Font.loadFont(
                    getClass().getResourceAsStream("/fonts/Horizon.otf"), 40);
            if (h != null) horizonFamily = h.getFamily();
        } catch (Exception ignored) {}

        try {
            Font g = Font.loadFont(
                    getClass().getResourceAsStream("/fonts/Garet-Book.ttf"), 13);
            if (g != null) garetFamily = g.getFamily();
        } catch (Exception ignored) {}

        try {
            Font l = Font.loadFont(
                    getClass().getResourceAsStream("/fonts/LeagueSpartan-Bold.ttf"), 16);
            if (l != null) leagueFamily = l.getFamily();
        } catch (Exception ignored) {}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layout builders
    // ─────────────────────────────────────────────────────────────────────────

    private VBox buildRoot() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG + ";");
        root.setAlignment(Pos.TOP_LEFT);
        root.setPadding(new Insets(48, 40, 40, 40));
        root.getChildren().addAll(buildHeader(), buildCard());
        return root;
    }

    // ── Header: NEWS AI / DETECTOR ───────────────────────────────────────────
    private VBox buildHeader() {

        // Row 1: "NEWS" (white) + " AI" (purple)
        Text tNEWS = new Text("NEWS");
        tNEWS.setFont(Font.font(horizonFamily, FontWeight.BOLD, 44));
        tNEWS.setFill(Color.WHITE);

        Text tAI = new Text(" AI");
        tAI.setFont(Font.font(horizonFamily, FontWeight.BOLD, 54));
        tAI.setFill(Color.web(PURPLE));

        TextFlow row1 = new TextFlow(tNEWS, tAI);

        // Row 2: "DETECTOR"
        Text tDETECTOR = new Text("DETECTOR");
        tDETECTOR.setFont(Font.font(horizonFamily, FontWeight.BOLD, 30));
        tDETECTOR.setFill(Color.WHITE);

        TextFlow row2 = new TextFlow(tDETECTOR);

        // Subtitle
        Label sub = new Label("Powered by OpenRouter API");
        sub.setFont(Font.font(garetFamily, 12));
        sub.setTextFill(Color.web(MUTED));
        sub.setPadding(new Insets(4, 0, 0, 0));

        VBox header = new VBox(2, row1, row2, sub);
        header.setPadding(new Insets(0, 0, 28, 0));
        return header;
    }

    // ── Card ─────────────────────────────────────────────────────────────────
    private VBox buildCard() {
        VBox card = new VBox(16);
        card.setStyle(
                "-fx-background-color: " + CARD + ";" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 16;"
        );
        card.setPadding(new Insets(24));

        // ── Input area ────────────────────────────────────────────────────
        inputArea = new TextArea();
        inputArea.setPromptText("Paste an Article or Headline over here");
        inputArea.setWrapText(true);
        inputArea.setPrefRowCount(5);
        inputArea.setFont(Font.font(garetFamily, 13));
        inputArea.setStyle(
                "-fx-control-inner-background: " + INPUT_BG + ";" +
                        "-fx-text-fill: " + WHITE + ";" +
                        "-fx-prompt-text-fill: " + MUTED + ";" +
                        "-fx-background-color: " + INPUT_BG + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;" +
                        "-fx-padding: 12;" +
                        "-fx-font-size: 13px;"
        );

        // ── Analyze button ────────────────────────────────────────────────
        analyzeBtn = new Button("Analyze");
        analyzeBtn.setMaxWidth(Double.MAX_VALUE);
        analyzeBtn.setPrefHeight(48);
        analyzeBtn.setFont(Font.font(leagueFamily, FontWeight.BOLD, 17));
        analyzeBtn.setTextFill(Color.WHITE);
        setButtonStyle(analyzeBtn, PURPLE);
        addHoverEffect(analyzeBtn);
        analyzeBtn.setOnAction(e -> runAnalysis());

        // ── Result box ────────────────────────────────────────────────────
        resultBox = buildResultBox();
        resultBox.setVisible(false);
        resultBox.setManaged(false);

        card.getChildren().addAll(inputArea, analyzeBtn, resultBox);
        return card;
    }

    private void setButtonStyle(Button btn, String color) {
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        );
    }

    // ── Result box ────────────────────────────────────────────────────────────
    private VBox buildResultBox() {
        VBox box = new VBox(10);
        box.setStyle(
                "-fx-background-color: " + INPUT_BG + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;"
        );
        box.setPadding(new Insets(16));

        // Verdict: REAL NEWS / FAKE NEWS
        verdictLabel = new Label("REAL NEWS");
        verdictLabel.setFont(Font.font(leagueFamily, FontWeight.BOLD, 18));
        verdictLabel.setTextFill(Color.web(GREEN));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + BORDER + "; -fx-padding: 0;");

        // Confidence
        confidenceLabel = new Label("AI Confidence - 0%");
        confidenceLabel.setFont(Font.font(garetFamily, 13));
        confidenceLabel.setTextFill(Color.web(MUTED));

        // Reason text area (scrollable)
        reasonArea = new TextArea();
        reasonArea.setWrapText(true);
        reasonArea.setEditable(false);
        reasonArea.setPrefRowCount(4);
        reasonArea.setFont(Font.font(garetFamily, 13));
        reasonArea.setStyle(
                "-fx-control-inner-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + WHITE + ";" +
                        "-fx-border-width: 0;" +
                        "-fx-background-insets: 0;" +
                        "-fx-padding: 4 0 0 0;" +
                        "-fx-font-size: 13px;"
        );

        // Wrap in ScrollPane so long text gets a scrollbar
        ScrollPane scroll = new ScrollPane(reasonArea);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(110);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;" +
                        "-fx-border-width: 0;" +
                        "-fx-padding: 0;"
        );

        box.getChildren().addAll(verdictLabel, sep, confidenceLabel, scroll);
        return box;
    }

    // ── Button hover: drop down 3px ───────────────────────────────────────────
    private void addHoverEffect(Button btn) {
        TranslateTransition down = new TranslateTransition(Duration.millis(140), btn);
        down.setToY(3);

        TranslateTransition up = new TranslateTransition(Duration.millis(140), btn);
        up.setToY(0);

        btn.setOnMouseEntered(e -> {
            setButtonStyle(btn, PURPLE_HOV);
            down.playFromStart();
        });
        btn.setOnMouseExited(e -> {
            setButtonStyle(btn, PURPLE);
            up.playFromStart();
        });
    }

    // ── Run analysis ──────────────────────────────────────────────────────────
    private void runAnalysis() {
        String text = inputArea.getText().strip();
        if (text.isEmpty()) return;

        analyzeBtn.setText("Analyzing...");
        analyzeBtn.setDisable(true);
        resultBox.setVisible(false);
        resultBox.setManaged(false);

        Task<FakeNewsDetector.DetectionResult> task = new Task<>() {
            @Override
            protected FakeNewsDetector.DetectionResult call() throws Exception {
                return FakeNewsDetector.detectNews(text);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> showResult(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> {
            analyzeBtn.setText("Analyze");
            analyzeBtn.setDisable(false);
            verdictLabel.setText("Error");
            verdictLabel.setTextFill(Color.web(RED));
            reasonArea.setText(task.getException().getMessage());
            showResultPanel();
        }));

        new Thread(task).start();
    }

    private void showResult(FakeNewsDetector.DetectionResult r) {
        analyzeBtn.setText("Analyze");
        analyzeBtn.setDisable(false);

        if (r.isFake()) {
            verdictLabel.setText("FAKE NEWS");
            verdictLabel.setTextFill(Color.web(RED));
        } else {
            verdictLabel.setText("REAL NEWS");
            verdictLabel.setTextFill(Color.web(GREEN));
        }

        confidenceLabel.setText("AI Confidence - " + r.confidence() + "%");
        reasonArea.setText(r.reason());
        reasonArea.setScrollTop(0);
        showResultPanel();
    }

    private void showResultPanel() {
        resultBox.setOpacity(0);
        resultBox.setTranslateY(16);
        resultBox.setVisible(true);
        resultBox.setManaged(true);

        Timeline anim = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(resultBox.opacityProperty(), 0),
                        new KeyValue(resultBox.translateYProperty(), 16)
                ),
                new KeyFrame(Duration.millis(380),
                        new KeyValue(resultBox.opacityProperty(), 1,  Interpolator.EASE_OUT),
                        new KeyValue(resultBox.translateYProperty(), 0, Interpolator.EASE_OUT)
                )
        );
        anim.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}