package com.railway.tracking.controller;

import com.railway.tracking.entity.TrainPosition;
import com.railway.tracking.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @GetMapping
    public ResponseEntity<List<TrainPosition>> getAllPositions() {
        return ResponseEntity.ok(trackingService.getAllPositions());
    }

    @GetMapping("/{trainId}")
    public ResponseEntity<TrainPosition> getPosition(@PathVariable Long trainId) {
        return ResponseEntity.ok(trackingService.getPosition(trainId));
    }
}
