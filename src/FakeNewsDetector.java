import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * AI-Powered Fake News Detector
 * Uses OpenRouter API to analyze news articles intelligently.
 *
 * Setup:
 *   Set your API key as an environment variable in IntelliJ:
 *   Run > Edit Configurations > Environment Variables:
 *   OPENROUTER_API_KEY=your_key_here
 *
 * Compile & Run:
 *   javac FakeNewsDetector.java
 *   java FakeNewsDetector
 */
public class FakeNewsDetector {

    private static final String API_URL  = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL    = "openrouter/auto"; // free model on OpenRouter
    private static final String API_KEY  = System.getenv("OPENROUTER_API_KEY");

    // ─── Public API ──────────────────────────────────────────────────────────

    /**
     * Sends the news text to the AI and returns a DetectionResult.
     */
    public static DetectionResult detectNews(String newsText) throws Exception {
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new IllegalStateException(
                    "OPENROUTER_API_KEY environment variable is not set.\n" +
                            "Add it in IntelliJ: Run > Edit Configurations > Environment Variables"
            );
        }

        String prompt = buildPrompt(newsText);
        String rawJson = callOpenRouterAPI(prompt);
        return parseResult(rawJson);
    }

    // ─── Prompt ──────────────────────────────────────────────────────────────

    private static String buildPrompt(String newsText) {
        return """
            You are an expert fact-checker and media literacy analyst.

            Analyze the following news article or headline and determine whether it appears
            to be REAL or FAKE news. Consider:
            - Sensationalist or clickbait language
            - Presence of verifiable claims
            - Emotional manipulation tactics
            - Source credibility signals
            - Logical consistency

            Respond ONLY in this exact JSON format (no extra text):
            {
              "verdict": "REAL" or "FAKE",
              "confidence": <integer 0-100>,
              "reason": "<one concise sentence explaining your verdict>",
              "warning_signs": ["<sign1>", "<sign2>"]
            }

            News to analyze:
            \"\"\"
            %s
            \"\"\"
            """.formatted(newsText);
    }

    // ─── HTTP ─────────────────────────────────────────────────────────────────

    private static String callOpenRouterAPI(String prompt) throws Exception {
        // Escape the prompt for JSON embedding
        String escaped = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        String requestBody = """
            {
              "model": "%s",
              "max_tokens": 512,
              "messages": [
                { "role": "user", "content": "%s" }
              ]
            }
            """.formatted(MODEL, escaped);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type",   "application/json")
                .header("Authorization",  "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API error " + response.statusCode() + ": " + response.body());
        }

        return extractTextFromResponse(response.body());
    }

    // ─── JSON helpers (no external deps) ─────────────────────────────────────

    /** Pulls the message content from OpenRouter's OpenAI-compatible response. */
    private static String extractTextFromResponse(String json) {
        // OpenRouter returns: choices[0].message.content
        String contentKey = "\"content\":";
        int idx = json.indexOf(contentKey);
        if (idx == -1) throw new RuntimeException("Unexpected API response: " + json);

        int start = json.indexOf('"', idx + contentKey.length()) + 1;
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') break;
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(++i);
                switch (next) {
                    case 'n'  -> sb.append('\n');
                    case 't'  -> sb.append('\t');
                    case '"'  -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    default   -> sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Parses the structured JSON returned by Claude into a DetectionResult. */
    private static DetectionResult parseResult(String json) {
        // Strip markdown fences if present
        json = json.replaceAll("```json|```", "").strip();

        String verdict    = extractJsonString(json, "verdict");
        int    confidence = extractJsonInt(json, "confidence");
        String reason     = extractJsonString(json, "reason");

        boolean isFake = verdict != null && verdict.trim().equalsIgnoreCase("FAKE");
        return new DetectionResult(isFake, confidence, reason);
    }

    private static String extractJsonString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return null;
        int colon = json.indexOf(':', idx + search.length());
        int q1    = json.indexOf('"', colon + 1);
        int q2    = json.indexOf('"', q1 + 1);
        return json.substring(q1 + 1, q2);
    }

    private static int extractJsonInt(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return 50;
        int colon = json.indexOf(':', idx + search.length());
        int start = colon + 1;
        while (start < json.length() && !Character.isDigit(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        try { return Integer.parseInt(json.substring(start, end)); }
        catch (NumberFormatException e) { return 50; }
    }

    // ─── Result record ────────────────────────────────────────────────────────

    public record DetectionResult(boolean isFake, int confidence, String reason) {
        @Override
        public String toString() {
            String verdict = isFake ? "FAKE NEWS ❌" : "REAL NEWS ✅";
            return """
                ┌─────────────────────────────────────┐
                  Verdict    : %s
                  Confidence : %d%%
                  Reason     : %s
                └─────────────────────────────────────┘
                """.formatted(verdict, confidence, reason);
        }
    }

    // ─── CLI entry point ──────────────────────────────────────────────────────

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║   AI Fake News Detector (CLI)   ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.println("Paste your news headline or article, then press Enter:");
        System.out.print("> ");

        String input = sc.nextLine().strip();
        if (input.isEmpty()) {
            System.out.println("No input provided. Exiting.");
            return;
        }

        System.out.println("\nAnalyzing with AI...\n");

        try {
            DetectionResult result = detectNews(input);
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}