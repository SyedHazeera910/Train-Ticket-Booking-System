package com.railway.ticketing.repository;

import com.railway.ticketing.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPnr(String pnr);
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findByTrainId(Long trainId);
}
