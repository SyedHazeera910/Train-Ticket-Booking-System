package com.railway.ticketing.service;

import com.railway.identity.entity.User;
import com.railway.identity.repository.UserRepository;
import com.railway.payments.entity.Wallet;
import com.railway.payments.repository.WalletRepository;
import com.railway.ticketing.dto.*;
import com.railway.ticketing.entity.Booking;
import com.railway.ticketing.entity.Train;
import com.railway.ticketing.repository.BookingRepository;
import com.railway.ticketing.repository.TrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TrainRepository trainRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    @Transactional
    public synchronized BookingResponse createBooking(BookingRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Train train = trainRepository.findById(request.getTrainId())
                .orElseThrow(() -> new RuntimeException("Train not found"));

        if (train.getAvailableSeats() < request.getNumberOfSeats()) {
            throw new RuntimeException("Not enough seats available");
        }

        BigDecimal fare = calculateFare(train.getBaseFare(), request.getTravelClass(), request.getNumberOfSeats());

        // Check wallet balance
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance().compareTo(fare) < 0) {
            throw new RuntimeException("Insufficient wallet balance");
        }

        // Deduct fare
        wallet.setBalance(wallet.getBalance().subtract(fare));
        walletRepository.save(wallet);

        // Reduce available seats
        train.setAvailableSeats(train.getAvailableSeats() - request.getNumberOfSeats());
        trainRepository.save(train);

        // Create booking
        Booking booking = Booking.builder()
                .user(user)
                .train(train)
                .pnr(generatePnr())
                .travelClass(request.getTravelClass())
                .numberOfSeats(request.getNumberOfSeats())
                .passengers(request.getPassengers())
                .status(Booking.BookingStatus.CONFIRMED)
                .fare(fare)
                .build();

        booking = bookingRepository.save(booking);
        return toResponse(booking);
    }

    public List<BookingResponse> getUserBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public BookingResponse getByPnr(String pnr) {
        Booking booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new RuntimeException("PNR not found"));
        return toResponse(booking);
    }

    @Transactional
    public void cancelBooking(Long id, String userEmail) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking already cancelled");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);

        // Refund 80%
        BigDecimal refund = booking.getFare().multiply(BigDecimal.valueOf(0.8));
        Wallet wallet = walletRepository.findByUserId(booking.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        wallet.setBalance(wallet.getBalance().add(refund));
        walletRepository.save(wallet);

        // Restore seats
        Train train = booking.getTrain();
        train.setAvailableSeats(train.getAvailableSeats() + booking.getNumberOfSeats());
        trainRepository.save(train);

        bookingRepository.save(booking);
    }

    private BigDecimal calculateFare(BigDecimal baseFare, String travelClass, int seats) {
        double multiplier = switch (travelClass) {
            case "AC1" -> 3.0;
            case "AC2" -> 2.0;
            case "AC3" -> 1.5;
            default -> 1.0; // SLEEPER
        };
        return baseFare.multiply(BigDecimal.valueOf(multiplier)).multiply(BigDecimal.valueOf(seats));
    }

    private String generatePnr() {
        return "PNR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BookingResponse toResponse(Booking b) {
        return new BookingResponse(
                b.getId(), b.getPnr(), b.getTrain().getName(), b.getTrain().getTrainNumber(),
                b.getTrain().getFromStation().getName(), b.getTrain().getToStation().getName(),
                b.getTrain().getDepartureTime(), b.getTrain().getArrivalTime(),
                b.getTravelClass(), b.getNumberOfSeats(), b.getPassengers(),
                b.getStatus().name(), b.getFare(), b.getCreatedAt()
        );
    }

    public List<Train> searchTrains(String from, String to, LocalDate date, int seats) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return trainRepository.searchTrains(from, to, start, end, seats);
    }
}
