package com.railway.tracking.service;

import com.railway.ticketing.repository.TrainRepository;
import com.railway.tracking.entity.TrainPosition;
import com.railway.tracking.repository.TrainPositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingService {

    private final TrainPositionRepository positionRepository;
    private final TrainRepository trainRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final Random random = new Random();

    public List<TrainPosition> getAllPositions() {
        return positionRepository.findAll();
    }

    public TrainPosition getPosition(Long trainId) {
        return positionRepository.findByTrainId(trainId)
                .orElseThrow(() -> new RuntimeException("No tracking data for train " + trainId));
    }

    /**
     * Simulates train movement every 5 seconds.
     * Updates each train's lat/lng and broadcasts via WebSocket.
     */
    @Scheduled(fixedRate = 5000)
    public void simulatePositions() {
        List<TrainPosition> positions = positionRepository.findAll();
        for (TrainPosition pos : positions) {
            // Small random drift to simulate movement
            pos.setLatitude(pos.getLatitude() + (random.nextDouble() - 0.5) * 0.01);
            pos.setLongitude(pos.getLongitude() + (random.nextDouble() - 0.5) * 0.01);
            pos.setSpeed(60.0 + random.nextInt(60));
            positionRepository.save(pos);

            // Broadcast to WebSocket subscribers
            messagingTemplate.convertAndSend("/topic/tracking/" + pos.getTrainId(), pos);
        }
    }
}
