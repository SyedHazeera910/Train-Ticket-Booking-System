package com.railway.chatbot.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Rule-based intent detector.
 * Matches user message text to a known intent using regex/keyword patterns.
 * No LLM API key required.
 *
 * Upgrade path: replace this class with a Spring AI ChatClient call that
 * returns a structured IntentResult — the ChatbotService won't need to change.
 */
@Component
public class IntentDetector {

    public enum Intent {
        PNR_STATUS,
        LIVE_TRACKING,
        MY_BOOKINGS,
        CANCEL_BOOKING,
        WALLET_BALANCE,
        FOOD_ORDER,
        FILE_COMPLAINT,
        HELP,
        GREETING,
        GENERAL_QUERY
    }

    // ── Patterns ────────────────────────────────────────────────────────────
    private static final Pattern PNR_PATTERN =
            Pattern.compile("\\bPNR[A-Z0-9]{6,10}\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern PNR_INTENT =
            Pattern.compile("\\b(pnr|status|booking status|check status|ticket status)\\b",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern TRACKING_INTENT =
            Pattern.compile("\\b(track|tracking|where is|live location|locate|train location|running status)\\b",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern MY_BOOKINGS_INTENT =
            Pattern.compile("\\b(my bookings?|my tickets?|show bookings?|list bookings?|booked trains?)\\b",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern CANCEL_INTENT =
            Pattern.compile("\\b(cancel|cancellation|refund|cancel ticket|cancel booking)\\b",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern WALLET_INTENT =
            Pattern.compile("\\b(wallet|balance|money|funds?|top.?up|recharge|credit|debit)\\b",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern FOOD_INTENT =
            Pattern.compile("\\b(food|meal|eat|snack|drink|lunch|dinner|breakfast|order food|menu)\\b",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern COMPLAINT_INTENT =
            Pattern.compile("\\b(complaint|complain|issue|problem|trouble|help me|not working|broken|wrong|damaged|lost|missing|delay|late)\\b",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern HELP_INTENT =
            Pattern.compile("\\b(help|what can you do|what are you|capabilities|features|options|commands)\\b",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern GREETING_INTENT =
            Pattern.compile("^\\s*(hi|hello|hey|good morning|good evening|good afternoon|howdy|namaste|hii+|helo)\\W*$",
                    Pattern.CASE_INSENSITIVE);

    // ── Detection ────────────────────────────────────────────────────────────

    /**
     * Returns the most likely intent for the given message.
     */
    public Intent detect(String message) {
        if (message == null || message.isBlank()) return Intent.GENERAL_QUERY;

        String msg = message.trim();

        // Greeting — check before others (short messages)
        if (GREETING_INTENT.matcher(msg).matches()) return Intent.GREETING;

        // Help request
        if (HELP_INTENT.matcher(msg).find()) return Intent.HELP;

        // PNR — explicit pattern match OR keywords
        if (PNR_PATTERN.matcher(msg).find() || PNR_INTENT.matcher(msg).find()) return Intent.PNR_STATUS;

        // Order of remaining checks matters (specificity first)
        if (CANCEL_INTENT.matcher(msg).find())    return Intent.CANCEL_BOOKING;
        if (TRACKING_INTENT.matcher(msg).find())  return Intent.LIVE_TRACKING;
        if (MY_BOOKINGS_INTENT.matcher(msg).find()) return Intent.MY_BOOKINGS;
        if (WALLET_INTENT.matcher(msg).find())    return Intent.WALLET_BALANCE;
        if (FOOD_INTENT.matcher(msg).find())      return Intent.FOOD_ORDER;
        if (COMPLAINT_INTENT.matcher(msg).find()) return Intent.FILE_COMPLAINT;

        return Intent.GENERAL_QUERY;
    }

    /**
     * Extracts a PNR number from the message text, if present.
     * Returns null if no PNR found.
     */
    public String extractPnr(String message) {
        var matcher = PNR_PATTERN.matcher(message.toUpperCase());
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * Extracts a numeric train number from the message text, if present.
     * Returns null if none found.
     */
    public String extractTrainNumber(String message) {
        var m = Pattern.compile("\\b(\\d{4,5})\\b").matcher(message);
        return m.find() ? m.group(1) : null;
    }
}
