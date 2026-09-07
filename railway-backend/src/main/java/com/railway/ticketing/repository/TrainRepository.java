package com.railway.ticketing.repository;

import com.railway.ticketing.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainRepository extends JpaRepository<Train, Long> {

    Optional<Train> findByTrainNumber(String trainNumber);

    @Query("SELECT t FROM Train t WHERE " +
           "t.fromStation.code = :from AND t.toStation.code = :to AND " +
           "t.availableSeats >= :seats")
    List<Train> findByRoute(
            @Param("from") String fromCode,
            @Param("to") String toCode,
            @Param("seats") int seats
    );
}

