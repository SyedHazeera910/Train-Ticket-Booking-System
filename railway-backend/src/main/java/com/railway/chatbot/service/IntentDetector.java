package com.railway.chatbot.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rule-based intent detector.
 * Matches user message text to a known intent using regex/keyword patterns.
 * No LLM API key required.
 */
@Component
public class IntentDetector {

    public enum Intent {
        BOOK_TICKET,
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
            Pattern.compile("\\b(pnr|pnr status|check pnr|check status|ticket status)\\b",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern BOOK_TICKET_INTENT =
            Pattern.compile("\\b(book|booking|reserve|reservation|search train|search trains|find train|find trains|train from|trains from|ticket from|tickets from|want to book|book ticket|book a ticket)\\b",
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
        if (CANCEL_INTENT.matcher(msg).find())      return Intent.CANCEL_BOOKING;
        if (BOOK_TICKET_INTENT.matcher(msg).find())  return Intent.BOOK_TICKET;
        if (TRACKING_INTENT.matcher(msg).find())    return Intent.LIVE_TRACKING;
        if (MY_BOOKINGS_INTENT.matcher(msg).find()) return Intent.MY_BOOKINGS;
        if (WALLET_INTENT.matcher(msg).find())      return Intent.WALLET_BALANCE;
        if (FOOD_INTENT.matcher(msg).find())        return Intent.FOOD_ORDER;
        if (COMPLAINT_INTENT.matcher(msg).find())   return Intent.FILE_COMPLAINT;

        return Intent.GENERAL_QUERY;
    }

    /**
     * Extracts a PNR number from the message text, if present.
     */
    public String extractPnr(String message) {
        var matcher = PNR_PATTERN.matcher(message.toUpperCase());
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * Extracts a numeric train number from the message text, if present.
     */
    public String extractTrainNumber(String message) {
        var m = Pattern.compile("\\b(\\d{4,5})\\b").matcher(message);
        return m.find() ? m.group(1) : null;
    }

    /**
     * Extracts route pair [fromStation, toStation] from message if present.
     * e.g., "from Delhi to Mumbai", "Delhi to Mumbai", "NDLS to BCT"
     */
    public String[] extractRoute(String message) {
        if (message == null) return null;

        // "from X to Y"
        Pattern p1 = Pattern.compile("from\\s+([A-Za-z\\s]+?)\\s+to\\s+([A-Za-z\\s]+?)(?:\\s+on|\\s+for|\\s+date|\\s+class|\\s*$)", Pattern.CASE_INSENSITIVE);
        Matcher m1 = p1.matcher(message);
        if (m1.find()) {
            return new String[]{m1.group(1).trim(), m1.group(2).trim()};
        }

        // "X to Y"
        Pattern p2 = Pattern.compile("([A-Za-z]+(?:\\s+[A-Za-z]+)?)\\s+to\\s+([A-Za-z]+(?:\\s+[A-Za-z]+)?)", Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(message);
        if (m2.find()) {
            String from = m2.group(1).trim().toLowerCase();
            String to = m2.group(2).trim().toLowerCase();
            // Filter out common verbs / non-station words preceding 'to'
            Set<String> nonStationVerbs = Set.of(
                "want", "like", "how", "where", "trying", "need", "wish", "going", "able", "access", "guide", "help", "agree", "refer", "listen"
            );
            if (!nonStationVerbs.contains(from) && !nonStationVerbs.contains(to)) {
                return new String[]{m2.group(1).trim(), m2.group(2).trim()};
            }
        }

        return null;
    }

    /**
     * Extracts travel class (AC1, AC2, AC3, SLEEPER) from message if present.
     */
    public String extractTravelClass(String message) {
        if (message == null) return "SLEEPER";
        String upper = message.toUpperCase();
        if (upper.contains("1AC") || upper.contains("AC1") || upper.contains("FIRST AC")) return "AC1";
        if (upper.contains("2AC") || upper.contains("AC2") || upper.contains("SECOND AC")) return "AC2";
        if (upper.contains("3AC") || upper.contains("AC3") || upper.contains("THIRD AC")) return "AC3";
        return "SLEEPER";
    }

    /**
     * Extracts travel date from message (e.g. YYYY-MM-DD or today / tomorrow).
     */
    public LocalDate extractTravelDate(String message) {
        if (message == null) return LocalDate.now().plusDays(1);
        String lower = message.toLowerCase();

        if (lower.contains("today")) return LocalDate.now();
        if (lower.contains("tomorrow")) return LocalDate.now().plusDays(1);

        Pattern p = Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2})\\b");
        Matcher m = p.matcher(message);
        if (m.find()) {
            try {
                return LocalDate.parse(m.group(1));
            } catch (Exception ignored) {}
        }
        return LocalDate.now().plusDays(1);
    }
}

