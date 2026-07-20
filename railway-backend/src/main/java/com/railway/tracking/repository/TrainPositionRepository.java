package com.railway.tracking.repository;

import com.railway.tracking.entity.TrainPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainPositionRepository extends JpaRepository<TrainPosition, Long> {
    Optional<TrainPosition> findByTrainId(Long trainId);
}
