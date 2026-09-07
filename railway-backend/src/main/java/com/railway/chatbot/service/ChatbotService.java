package com.railway.chatbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.railway.chatbot.dto.ChatRequest;
import com.railway.chatbot.dto.ChatResponse;
import com.railway.chatbot.entity.ChatSession;
import com.railway.chatbot.repository.ChatSessionRepository;
import com.railway.chatbot.service.IntentDetector.Intent;
import com.railway.payments.service.WalletService;
import com.railway.support.entity.SupportTicket;
import com.railway.support.repository.SupportTicketRepository;
import com.railway.ticketing.dto.BookingRequest;
import com.railway.ticketing.dto.BookingResponse;
import com.railway.ticketing.entity.Station;
import com.railway.ticketing.entity.Train;
import com.railway.ticketing.repository.StationRepository;
import com.railway.ticketing.repository.TrainRepository;
import com.railway.ticketing.service.BookingService;
import com.railway.tracking.entity.TrainPosition;
import com.railway.tracking.service.TrackingService;
import com.railway.identity.entity.User;
import com.railway.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final IntentDetector intentDetector;
    private final ChatSessionRepository sessionRepository;
    private final BookingService bookingService;
    private final StationRepository stationRepository;
    private final TrainRepository trainRepository;
    private final WalletService walletService;
    private final TrackingService trackingService;
    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /** Maximum number of conversation turns to keep in history */
    private static final int MAX_HISTORY_TURNS = 20;

    @Transactional
    public ChatResponse handleMessage(String userEmail, ChatRequest request) {
        String sessionId = (request.getSessionId() != null && !request.getSessionId().isBlank())
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        // Load or create session
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseGet(() -> ChatSession.builder()
                        .sessionId(sessionId)
                        .userEmail(userEmail)
                        .historyJson("[]")
                        .build());

        String userMessage = request.getMessage() == null ? "" : request.getMessage().trim();

        // Detect intent
        Intent intent = intentDetector.detect(userMessage);
        log.debug("Chatbot intent={} for user={}", intent, userEmail);

        // Handle intent → generate reply
        String reply;
        try {
            reply = switch (intent) {
                case GREETING      -> handleGreeting(userEmail);
                case HELP          -> handleHelp();
                case BOOK_TICKET   -> handleBookTicket(userEmail, userMessage, session);
                case PNR_STATUS    -> handlePnrStatus(userEmail, userMessage);
                case LIVE_TRACKING -> handleTracking(userMessage);
                case MY_BOOKINGS   -> handleMyBookings(userEmail);
                case CANCEL_BOOKING -> handleCancelInfo();
                case WALLET_BALANCE -> handleWalletBalance(userEmail);
                case FOOD_ORDER    -> handleFoodInfo();
                case FILE_COMPLAINT -> handleComplaint(userEmail, userMessage, session);
                default            -> handleGeneralQuery(userMessage);
            };
        } catch (Exception e) {
            log.warn("Chatbot error handling intent {}: {}", intent, e.getMessage());
            reply = "Sorry, I couldn't fetch that information right now. Please try again or visit the relevant page. 🙏";
        }

        // Persist history
        appendToHistory(session, userMessage, reply);
        sessionRepository.save(session);

        return new ChatResponse(reply, intent.name(), sessionId);
    }

    // ── Intent Handlers ──────────────────────────────────────────────────────

    private String handleGreeting(String userEmail) {
        String firstName = "there";
        try {
            var userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                firstName = userOpt.get().getName().split(" ")[0];
            }
        } catch (Exception ignored) {}
        return String.format(
                "👋 Hi %s! I'm **RailBot**, your AI travel assistant.\n\n" +
                "I can help you with:\n" +
                "• 🎫 **Book Tickets** — search trains & book directly in chat\n" +
                "• 🎟️ **PNR Status** — check your ticket status\n" +
                "• 🚂 **Live Train Tracking** — mention your train number\n" +
                "• 📋 **My Bookings** — see your recent bookings\n" +
                "• 💰 **Wallet Balance** — check your RailWallet funds\n" +
                "• 🍱 **Food Menu** — order meals on your train\n" +
                "• 🆘 **File a Complaint** — raise a support ticket\n\n" +
                "What can I help you with today?", firstName);
    }

    private String handleHelp() {
        return "🤖 **RailBot — What I can do:**\n\n" +
               "• **Book Ticket**: \"Book ticket from Delhi to Mumbai\"\n" +
               "• **PNR Status**: \"What is the status of PNR ABC12345?\"\n" +
               "• **Track Train**: \"Where is train 12345 right now?\"\n" +
               "• **My Bookings**: \"Show my recent bookings\"\n" +
               "• **Wallet**: \"What is my wallet balance?\"\n" +
               "• **Food**: \"What food is available on my train?\"\n" +
               "• **Cancel**: \"How do I cancel my ticket?\"\n" +
               "• **Complaint**: \"I have a problem with my booking\"\n\n" +
               "Just type naturally — I'll understand! 😊";
    }

    private String handleBookTicket(String userEmail, String message, ChatSession session) {
        String userMsg = message == null ? "" : message.trim();

        // Check if user is issuing a booking confirmation command like "book train 12951 AC1"
        String trainNum = intentDetector.extractTrainNumber(userMsg);
        boolean containsBookWord = Pattern.compile("\\b(book|reserve|confirm)\\b", Pattern.CASE_INSENSITIVE).matcher(userMsg).find();

        if (trainNum != null && containsBookWord) {
            return processChatBookingExecution(userEmail, userMsg, trainNum);
        }

        // Try to extract route [from, to]
        String[] route = intentDetector.extractRoute(userMsg);
        if (route == null) {
            return "🎫 **RailBot Ticket Booking Assistant**\n\n" +
                   "I can help you search and book train tickets directly!\n\n" +
                   "Please specify your **Source** and **Destination** stations.\n" +
                   "**Examples:**\n" +
                   "• *\"Book ticket from Delhi to Mumbai\"*\n" +
                   "• *\"Search train NDLS to BCT on 2026-08-15\"*\n" +
                   "• *\"Train from Chennai to Hyderabad\"*\n\n" +
                   "📍 **Available Stations:** New Delhi (NDLS), Mumbai Central (BCT), Chennai (MAS), Kolkata (HWH), Jaipur (JP), Hyderabad (HYB), Bengaluru (SBC), Ahmedabad (ADI).";
        }

        String fromRaw = route[0];
        String toRaw = route[1];

        // Resolve stations to Station entities or codes
        Station fromStation = resolveStation(fromRaw);
        Station toStation = resolveStation(toRaw);

        if (fromStation == null || toStation == null) {
            boolean looksLikeGenericPrompt = !userMsg.toLowerCase().contains("from") &&
                    (fromRaw.toLowerCase().contains("want") || fromRaw.toLowerCase().contains("book") ||
                     toRaw.toLowerCase().contains("book") || toRaw.toLowerCase().contains("ticket"));

            if (looksLikeGenericPrompt) {
                return "🎫 **RailBot Ticket Booking Assistant**\n\n" +
                       "I can help you search and book train tickets directly!\n\n" +
                       "Please specify your **Source** and **Destination** stations.\n" +
                       "**Examples:**\n" +
                       "• *\"Book ticket from Delhi to Mumbai\"*\n" +
                       "• *\"Search train NDLS to BCT on 2026-08-15\"*\n" +
                       "• *\"Train from Chennai to Hyderabad\"*\n\n" +
                       "📍 **Available Stations:** New Delhi (NDLS), Mumbai Central (BCT), Chennai (MAS), Kolkata (HWH), Jaipur (JP), Hyderabad (HYB), Bengaluru (SBC), Ahmedabad (ADI).";
            }

            String missing = (fromStation == null ? "**" + fromRaw + "**" : "") +
                             (fromStation == null && toStation == null ? " and " : "") +
                             (toStation == null ? "**" + toRaw + "**" : "");
            return String.format("❌ Could not locate station %s in our system.\n\n" +
                   "Please try standard station names or codes like **NDLS** (Delhi), **BCT** (Mumbai), **MAS** (Chennai), **HWH** (Kolkata), **JP** (Jaipur), **HYB** (Hyderabad), **SBC** (Bengaluru).", missing);
        }

        LocalDate travelDate = intentDetector.extractTravelDate(userMsg);
        List<Train> trains = bookingService.searchTrains(fromStation.getCode(), toStation.getCode(), travelDate, 1);

        if (trains.isEmpty()) {
            return String.format("🔍 No active trains found running from **%s (%s)** to **%s (%s)** on **%s**.\n\n" +
                   "Please check back or try searching for a different date or route! 🚂",
                   fromStation.getName(), fromStation.getCode(),
                   toStation.getName(), toStation.getCode(),
                   travelDate);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("🚄 **Found %d Train(s) from %s to %s** on %s:\n\n",
                trains.size(), fromStation.getName(), toStation.getName(), travelDate));

        for (int i = 0; i < trains.size(); i++) {
            Train t = trains.get(i);
            BigDecimal sleeperFare = t.getBaseFare();
            BigDecimal ac3Fare = t.getBaseFare().multiply(BigDecimal.valueOf(1.5));
            BigDecimal ac2Fare = t.getBaseFare().multiply(BigDecimal.valueOf(2.0));
            BigDecimal ac1Fare = t.getBaseFare().multiply(BigDecimal.valueOf(3.0));

            sb.append(String.format(
                    "**%d. %s (%s)** — %s\n" +
                    "   ⏰ Departs: %s | Arrives: %s\n" +
                    "   💺 Available Seats: **%d**\n" +
                    "   💰 Fares: SL: ₹%.0f | AC3: ₹%.0f | AC2: ₹%.0f | AC1: ₹%.0f\n" +
                    "   👉 *To book, reply:* **\"book train %s SLEEPER\"** or **\"book train %s AC3\"**\n\n",
                    i + 1, t.getName(), t.getTrainNumber(), t.getTrainType(),
                    t.getDepartureTime().toLocalTime(), t.getArrivalTime().toLocalTime(),
                    t.getAvailableSeats(),
                    sleeperFare, ac3Fare, ac2Fare, ac1Fare,
                    t.getTrainNumber(), t.getTrainNumber()
            ));
        }

        sb.append("💳 Bookings are instantly confirmed using your **RailWallet** balance!");
        return sb.toString();
    }

    private String processChatBookingExecution(String userEmail, String userMsg, String trainNum) {
        try {
            Train train = trainRepository.findByTrainNumber(trainNum)
                    .orElseThrow(() -> new RuntimeException("Train " + trainNum + " not found"));

            String travelClass = intentDetector.extractTravelClass(userMsg);
            LocalDate travelDate = intentDetector.extractTravelDate(userMsg);

            // Passenger details
            String passengerName = "Traveler";
            try {
                var userOpt = userRepository.findByEmail(userEmail);
                if (userOpt.isPresent() && userOpt.get().getName() != null) {
                    passengerName = userOpt.get().getName();
                }
            } catch (Exception ignored) {}

            BookingRequest req = BookingRequest.builder()
                    .trainId(train.getId())
                    .travelClass(travelClass)
                    .numberOfSeats(1)
                    .passengers(List.of(passengerName + "|30|M"))
                    .travelDate(travelDate)
                    .build();

            BookingResponse booking = bookingService.createBooking(req, userEmail);

            return String.format(
                    "🎉 **TICKET BOOKED SUCCESSFULLY!**\n\n" +
                    "🎫 **PNR:** **%s**\n" +
                    "🚂 Train: %s (%s)\n" +
                    "📍 Route: %s → %s\n" +
                    "📅 Travel Date: %s\n" +
                    "💺 Class: **%s** | Passenger: %s\n" +
                    "💰 Fare Paid: **₹%.2f** (Debited from RailWallet)\n" +
                    "✅ Status: **CONFIRMED**\n\n" +
                    "View all your active bookings under **[My Bookings](/bookings)** 📋",
                    booking.getPnr(),
                    booking.getTrainName(), booking.getTrainNumber(),
                    booking.getFromStation(), booking.getToStation(),
                    booking.getTravelDate(),
                    booking.getTravelClass(), passengerName,
                    booking.getFare()
            );
        } catch (Exception e) {
            log.error("Failed to book ticket via chatbot: {}", e.getMessage());
            return String.format("❌ **Booking Failed:** %s\n\n" +
                   "Please ensure you have enough balance in your **[RailWallet](/wallet)** or select a valid train.",
                   e.getMessage());
        }
    }

    private Station resolveStation(String query) {
        if (query == null || query.isBlank()) return null;
        String q = query.trim();
        // Exact code match
        Optional<Station> byCode = stationRepository.findByCode(q.toUpperCase());
        if (byCode.isPresent()) return byCode.get();

        // Search by name or city
        List<Station> matches = stationRepository.findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(q, q);
        if (!matches.isEmpty()) {
            return matches.get(0);
        }
        return null;
    }



    private String handlePnrStatus(String userEmail, String message) {
        String pnr = intentDetector.extractPnr(message);
        if (pnr == null) {
            return "Sure! Please share your **PNR number** and I'll check the status for you.\n" +
                   "Example: \"Status of PNR PNR1A2B3C4D\"";
        }
        try {
            var booking = bookingService.getByPnr(pnr);
            return String.format(
                    "🎫 **PNR Status: %s**\n\n" +
                    "🚂 Train: %s (%s)\n" +
                    "📍 Route: %s → %s\n" +
                    "🕐 Departure: %s\n" +
                    "💺 Class: %s | Seats: %d\n" +
                    "💰 Fare Paid: ₹%.2f\n" +
                    "✅ Status: **%s**",
                    pnr,
                    booking.getTrainName(), booking.getTrainNumber(),
                    booking.getFromStation(), booking.getToStation(),
                    booking.getDepartureTime(),
                    booking.getTravelClass(), booking.getNumberOfSeats(),
                    booking.getFare(),
                    booking.getStatus());
        } catch (Exception e) {
            return String.format("❌ I couldn't find any booking with PNR **%s**.\n" +
                    "Please double-check the PNR number and try again.", pnr);
        }
    }

    private String handleTracking(String message) {
        String trainNum = intentDetector.extractTrainNumber(message);
        if (trainNum == null) {
            // List all tracked trains
            try {
                var positions = trackingService.getAllPositions();
                if (positions.isEmpty()) {
                    return "🚂 No trains are currently being tracked.\n" +
                           "Please visit the [Live Tracking](/tracking) page for the full map view.";
                }
                StringBuilder sb = new StringBuilder("🗺️ **Live Train Positions:**\n\n");
                for (TrainPosition pos : positions) {
                    sb.append(String.format("• Train ID **%d** — Speed: %.0f km/h | Lat: %.4f, Lon: %.4f\n",
                            pos.getTrainId(), pos.getSpeed(), pos.getLatitude(), pos.getLongitude()));
                }
                sb.append("\nFor the interactive map, visit the **[Live Tracking](/tracking)** page! 🗺️");
                return sb.toString();
            } catch (Exception e) {
                return "📡 Please visit the **[Live Tracking](/tracking)** page to see all train positions on the map.";
            }
        }

        try {
            Long trainId = Long.parseLong(trainNum);
            TrainPosition pos = trackingService.getPosition(trainId);
            return String.format(
                    "📡 **Live Position — Train %s**\n\n" +
                    "📍 Latitude: %.4f\n" +
                    "📍 Longitude: %.4f\n" +
                    "🚀 Speed: %.0f km/h\n\n" +
                    "🗺️ Open the **[Live Tracking](/tracking)** page for the full map view!",
                    trainNum, pos.getLatitude(), pos.getLongitude(), pos.getSpeed());
        } catch (Exception e) {
            return String.format("❌ No tracking data found for train **%s**.\n" +
                    "Try visiting the **[Live Tracking](/tracking)** page for the interactive map.", trainNum);
        }
    }

    private String handleMyBookings(String userEmail) {
        try {
            var bookings = bookingService.getUserBookings(userEmail);
            if (bookings.isEmpty()) {
                return "📋 You don't have any bookings yet.\n\n" +
                       "Ready to plan your journey? Visit **[Search Trains](/search)** to find and book a train! 🚂";
            }
            // Show latest 3
            int count = Math.min(bookings.size(), 3);
            StringBuilder sb = new StringBuilder(
                    String.format("📋 **Your Recent Bookings** (%d shown):\n\n", count));
            for (int i = 0; i < count; i++) {
                var b = bookings.get(i);
                sb.append(String.format(
                        "%d. 🎫 PNR: **%s**\n" +
                        "   🚂 %s → %s\n" +
                        "   💺 %s | ₹%.2f | %s\n\n",
                        i + 1, b.getPnr(),
                        b.getFromStation(), b.getToStation(),
                        b.getTravelClass(), b.getFare(),
                        b.getStatus()));
            }
            if (bookings.size() > 3) {
                sb.append(String.format("...and %d more. See all on **[My Bookings](/bookings)** →",
                        bookings.size() - 3));
            }
            return sb.toString();
        } catch (Exception e) {
            return "📋 Couldn't load your bookings right now.\n" +
                   "Please check the **[My Bookings](/bookings)** page directly.";
        }
    }

    private String handleCancelInfo() {
        return "❌ **Cancellation Policy:**\n\n" +
               "• You can cancel any **confirmed** booking\n" +
               "• A **20% cancellation fee** is charged\n" +
               "• Remaining **80%** is refunded to your RailWallet\n" +
               "• Refund is instant — no waiting!\n\n" +
               "👉 Go to **[My Bookings](/bookings)** and click **Cancel** on the booking you want to cancel.";
    }

    private String handleWalletBalance(String userEmail) {
        try {
            var wallet = walletService.getWallet(userEmail);
            String balanceStr = String.format("₹%.2f", wallet.getBalance());
            String txnSummary = "";
            var txns = wallet.getTransactions();
            if (txns != null && !txns.isEmpty()) {
                var latest = txns.get(0);
                String sign = "CREDIT".equals(latest.getType()) ? "+" : "-";
                txnSummary = String.format("\n\n📊 Latest transaction: **%s₹%.2f** — %s",
                        sign, latest.getAmount(), latest.getDescription());
            }
            return String.format(
                    "💰 **Your RailWallet Balance**\n\n" +
                    "Available: **%s**%s\n\n" +
                    "Need to top up? Visit **[My Wallet](/wallet)** to add funds! 💳",
                    balanceStr, txnSummary);
        } catch (Exception e) {
            return "💰 Couldn't fetch your wallet balance right now.\n" +
                   "Please visit the **[Wallet](/wallet)** page directly.";
        }
    }

    private String handleFoodInfo() {
        return "🍱 **Food Ordering**\n\n" +
               "Order fresh meals delivered to your seat!\n\n" +
               "**Available Categories:**\n" +
               "• 🍛 North Indian\n" +
               "• 🍚 South Indian\n" +
               "• 🥗 Snacks & Light Bites\n" +
               "• 🧃 Beverages\n" +
               "• 🍰 Desserts\n\n" +
               "👉 Visit **[Food Order](/food)** to browse the full menu and place your order!";
    }

    private String handleComplaint(String userEmail, String message, ChatSession session) {
        // Check if this looks like a complaint description (previous turn may have asked for it)
        List<Map<String, String>> history = parseHistory(session.getHistoryJson());
        boolean askedForDescription = !history.isEmpty() &&
                history.get(history.size() - 1).getOrDefault("role", "").equals("bot") &&
                history.get(history.size() - 1).getOrDefault("text", "").contains("describe");

        if (askedForDescription && message.length() > 15) {
            // Create a complaint ticket automatically
            try {
                User user = userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                SupportTicket ticket = SupportTicket.builder()
                        .user(user)
                        .subject("Chatbot Complaint")
                        .description(message)
                        .status(SupportTicket.TicketStatus.OPEN)
                        .build();
                SupportTicket saved = supportTicketRepository.save(ticket);
                return String.format(
                        "✅ **Complaint Filed Successfully!**\n\n" +
                        "🎫 Ticket #%d has been created.\n" +
                        "Our support team will review it and get back to you shortly.\n\n" +
                        "📋 You can track your ticket at **[Help & Support](/support)** →",
                        saved.getId());
            } catch (Exception e) {
                return "❌ Couldn't file the complaint automatically. Please visit **[Help & Support](/support)** to raise a ticket manually.";
            }
        }

        return "🆘 **I'm sorry to hear you're having a problem!**\n\n" +
               "Could you briefly **describe your issue**? (e.g., wrong charge, delayed refund, lost luggage)\n\n" +
               "I'll create a support ticket for you right away, or you can visit **[Help & Support](/support)** directly.";
    }

    private String handleGeneralQuery(String message) {
        return "🤖 I'm trained to help with **railway-specific queries**.\n\n" +
               "Here's what I can do:\n" +
               "• 🎫 Check PNR status\n" +
               "• 🚂 Track live train position\n" +
               "• 📋 View your bookings\n" +
               "• 💰 Check wallet balance\n" +
               "• 🍱 Browse food menu\n" +
               "• 🆘 File a complaint\n\n" +
               "Type **'help'** to see example questions, or feel free to ask anything railway-related! 😊";
    }

    // ── History Management ───────────────────────────────────────────────────

    private void appendToHistory(ChatSession session, String userMessage, String botReply) {
        List<Map<String, String>> history = parseHistory(session.getHistoryJson());

        history.add(Map.of("role", "user", "text", userMessage));
        history.add(Map.of("role", "bot",  "text", botReply));

        // Cap at MAX_HISTORY_TURNS pairs (each pair = 2 entries)
        while (history.size() > MAX_HISTORY_TURNS * 2) {
            history.remove(0);
        }

        try {
            session.setHistoryJson(objectMapper.writeValueAsString(history));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize chat history", e);
            session.setHistoryJson("[]");
        }
    }

    private List<Map<String, String>> parseHistory(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
