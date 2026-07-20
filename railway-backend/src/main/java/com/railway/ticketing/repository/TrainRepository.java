package com.railway.ticketing.repository;

import com.railway.ticketing.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TrainRepository extends JpaRepository<Train, Long> {

    @Query("SELECT t FROM Train t WHERE " +
           "t.fromStation.code = :from AND t.toStation.code = :to AND " +
           "t.departureTime >= :startOfDay AND t.departureTime < :endOfDay AND " +
           "t.availableSeats >= :seats")
    List<Train> searchTrains(
            @Param("from") String fromCode,
            @Param("to") String toCode,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("seats") int seats
    );
}
