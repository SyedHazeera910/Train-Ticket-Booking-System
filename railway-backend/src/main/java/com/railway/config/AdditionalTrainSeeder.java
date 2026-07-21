package com.railway.config;

import com.railway.ticketing.entity.RunningFrequency;
import com.railway.ticketing.entity.Station;
import com.railway.ticketing.entity.Train;
import com.railway.ticketing.repository.StationRepository;
import com.railway.ticketing.repository.TrainRepository;
import com.railway.tracking.entity.TrainPosition;
import com.railway.tracking.repository.TrainPositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdditionalTrainSeeder {

    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final TrainPositionRepository positionRepository;

    @Bean
    public ApplicationRunner seedAdditionalTrains() {
        return args -> {
            addIfMissing("12951-2", "Rajdhani Express (Evening)", "NDLS", "BCT", "RAJDHANI",
                    16, 25, 1, 8, 15, 1850, 500, RunningFrequency.DAILY, Set.of(DayOfWeek.values()),
                    28.6139, 77.2090);

            addIfMissing("12004", "Shatabdi Express (Lucknow)", "NDLS", "LKO", "SHATABDI",
                    6, 10, 0, 12, 25, 950, 300, RunningFrequency.DAILY, Set.of(DayOfWeek.values()),
                    28.6139, 77.2090);

            addIfMissing("12273", "Duronto Express (Kolkata-Chennai)", "HWH", "MAS", "EXPRESS",
                    11, 30, 1, 14, 45, 2100, 450, RunningFrequency.DAILY, Set.of(DayOfWeek.values()),
                    22.5726, 88.3639);

            addIfMissing("11007", "Jan Shatabdi Express", "PUNE", "BCT", "EXPRESS",
                    5, 50, 0, 9, 10, 350, 400, RunningFrequency.DAILY, Set.of(DayOfWeek.values()),
                    18.5204, 73.8567);

            addIfMissing("12628", "Karnataka Express (Return)", "SBC", "NDLS", "EXPRESS",
                    19, 20, 2, 5, 40, 2350, 600, RunningFrequency.DAILY, Set.of(DayOfWeek.values()),
                    12.9716, 77.5946);
        };
    }

    private void addIfMissing(String trainNumber, String name, String fromCode, String toCode,
                               String trainType, int depHour, int depMin, int depDayOffset,
                               int arrHour, int arrMin, int baseFare, int seats,
                               RunningFrequency frequency, Set<DayOfWeek> runningDays,
                               double lat, double lon) {
        if (trainRepository.findAll().stream().anyMatch(t -> t.getTrainNumber().equals(trainNumber))) {
            return;
        }
        Station from = stationRepository.findByCode(fromCode).orElse(null);
        Station to = stationRepository.findByCode(toCode).orElse(null);
        if (from == null || to == null) {
            log.warn("Skipping train {} — station not found", trainNumber);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Train t = Train.builder()
                .name(name).trainNumber(trainNumber).trainType(trainType)
                .fromStation(from).toStation(to)
                .departureTime(now.plusDays(depDayOffset).withHour(depHour).withMinute(depMin))
                .arrivalTime(now.plusDays(depDayOffset + (arrHour < depHour ? 1 : 0)).plusDays(0)
                        .withHour(arrHour).withMinute(arrMin))
                .totalSeats(seats).availableSeats(seats)
                .baseFare(BigDecimal.valueOf(baseFare))
                .latitude(lat).longitude(lon)
                .frequency(frequency).runningDays(runningDays)
                .build();

        Train saved = trainRepository.save(t);
        log.info("Seeded additional train: {} ({})", name, trainNumber);

        if (positionRepository.findByTrainId(saved.getId()).isEmpty()) {
            positionRepository.save(TrainPosition.builder()
                    .trainId(saved.getId())
                    .latitude(lat).longitude(lon)
                    .speed(75.0)
                    .currentStation(from.getName())
                    .build());
        }
    }
}
