package com.railway.config;

import com.railway.food.entity.FoodItem;
import com.railway.food.repository.FoodItemRepository;
import com.railway.ticketing.entity.Station;
import com.railway.ticketing.entity.Train;
import com.railway.ticketing.repository.StationRepository;
import com.railway.ticketing.repository.TrainRepository;
import com.railway.tracking.entity.TrainPosition;
import com.railway.tracking.repository.TrainPositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final StationRepository stationRepository;
    private final TrainRepository trainRepository;
    private final TrainPositionRepository positionRepository;
    private final FoodItemRepository foodItemRepository;

    @Override
    public void run(String... args) {
        if (stationRepository.count() > 0) {
            log.info("Data already seeded, skipping...");
            return;
        }

        log.info("Seeding initial data...");
        seedStations();
        seedTrains();
        seedFoodItems();
        log.info("Data seeding complete!");
    }

    private void seedStations() {
        List<Station> stations = List.of(
            Station.builder().name("New Delhi").code("NDLS").city("Delhi").state("Delhi").build(),
            Station.builder().name("Mumbai Central").code("BCT").city("Mumbai").state("Maharashtra").build(),
            Station.builder().name("Chennai Central").code("MAS").city("Chennai").state("Tamil Nadu").build(),
            Station.builder().name("Kolkata Howrah").code("HWH").city("Kolkata").state("West Bengal").build(),
            Station.builder().name("Bengaluru City").code("SBC").city("Bengaluru").state("Karnataka").build(),
            Station.builder().name("Hyderabad Deccan").code("HYB").city("Hyderabad").state("Telangana").build(),
            Station.builder().name("Jaipur Junction").code("JP").city("Jaipur").state("Rajasthan").build(),
            Station.builder().name("Ahmedabad Junction").code("ADI").city("Ahmedabad").state("Gujarat").build(),
            Station.builder().name("Pune Junction").code("PUNE").city("Pune").state("Maharashtra").build(),
            Station.builder().name("Lucknow NR").code("LKO").city("Lucknow").state("Uttar Pradesh").build(),
            Station.builder().name("Pinakini ").code("PIN").city("Chennai").state("Tamil Nadu").build()
        );
        stationRepository.saveAll(stations);
    }

    private void seedTrains() {
        Station delhi = stationRepository.findByCode("NDLS").orElseThrow();
        Station mumbai = stationRepository.findByCode("BCT").orElseThrow();
        Station chennai = stationRepository.findByCode("MAS").orElseThrow();
        Station kolkata = stationRepository.findByCode("HWH").orElseThrow();
        Station bengaluru = stationRepository.findByCode("SBC").orElseThrow();
        Station hyderabad = stationRepository.findByCode("HYB").orElseThrow();
        Station jaipur = stationRepository.findByCode("JP").orElseThrow();
        Station ahmedabad = stationRepository.findByCode("ADI").orElseThrow();

        LocalDateTime now = LocalDateTime.now();

        List<Train> trains = List.of(
            Train.builder()
                .name("Rajdhani Express").trainNumber("12951").trainType("RAJDHANI")
                .fromStation(delhi).toStation(mumbai)
                .departureTime(now.plusDays(1).withHour(16).withMinute(25))
                .arrivalTime(now.plusDays(2).withHour(8).withMinute(15))
                .totalSeats(500).availableSeats(320).baseFare(BigDecimal.valueOf(850))
                .latitude(28.6139).longitude(77.2090).build(),

            Train.builder()
                .name("Shatabdi Express").trainNumber("12001").trainType("SHATABDI")
                .fromStation(delhi).toStation(jaipur)
                .departureTime(now.plusDays(1).withHour(6).withMinute(5))
                .arrivalTime(now.plusDays(1).withHour(10).withMinute(40))
                .totalSeats(300).availableSeats(185).baseFare(BigDecimal.valueOf(550))
                .latitude(28.6139).longitude(77.2090).build(),

            Train.builder()
                .name("Duronto Express").trainNumber("12213").trainType("EXPRESS")
                .fromStation(mumbai).toStation(delhi)
                .departureTime(now.plusDays(1).withHour(23).withMinute(0))
                .arrivalTime(now.plusDays(2).withHour(21).withMinute(30))
                .totalSeats(450).availableSeats(210).baseFare(BigDecimal.valueOf(900))
                .latitude(19.0760).longitude(72.8777).build(),

            Train.builder()
                .name("Coromandel Express").trainNumber("12841").trainType("EXPRESS")
                .fromStation(kolkata).toStation(chennai)
                .departureTime(now.plusDays(1).withHour(14).withMinute(20))
                .arrivalTime(now.plusDays(2).withHour(22).withMinute(5))
                .totalSeats(500).availableSeats(95).baseFare(BigDecimal.valueOf(780))
                .latitude(22.5726).longitude(88.3639).build(),

            Train.builder()
                .name("Karnataka Express").trainNumber("12627").trainType("EXPRESS")
                .fromStation(delhi).toStation(bengaluru)
                .departureTime(now.plusDays(1).withHour(22).withMinute(30))
                .arrivalTime(now.plusDays(3).withHour(6).withMinute(45))
                .totalSeats(600).availableSeats(430).baseFare(BigDecimal.valueOf(1100))
                .latitude(28.6139).longitude(77.2090).build(),

            Train.builder()
                .name("Deccan Queen").trainNumber("12123").trainType("EXPRESS")
                .fromStation(hyderabad).toStation(mumbai)
                .departureTime(now.plusDays(1).withHour(7).withMinute(15))
                .arrivalTime(now.plusDays(1).withHour(20).withMinute(40))
                .totalSeats(350).availableSeats(270).baseFare(BigDecimal.valueOf(650))
                .latitude(17.3850).longitude(78.4867).build(),

            Train.builder()
                .name("Gujarat Queen").trainNumber("12901").trainType("RAJDHANI")
                .fromStation(ahmedabad).toStation(delhi)
                .departureTime(now.plusDays(2).withHour(8).withMinute(0))
                .arrivalTime(now.plusDays(2).withHour(21).withMinute(55))
                .totalSeats(400).availableSeats(150).baseFare(BigDecimal.valueOf(720))
                .latitude(23.0225).longitude(72.5714).build(),

                 Train.builder()
                .name("Pinakini ").trainNumber("12933").trainType("SuperFast Express")
                .fromStation(chennai).toStation(hyderabad)
                .departureTime(now.plusDays(1).withHour(10).withMinute(0))
                .arrivalTime(now.plusDays(1).withHour(20).withMinute(07))
                .totalSeats(350).availableSeats(150).baseFare(BigDecimal.valueOf(520))
                .latitude(27.0225).longitude(65.5714).build()
        );

        List<Train> saved = trainRepository.saveAll(trains);

        // Seed positions for each train
        for (Train t : saved) {
            if (positionRepository.findByTrainId(t.getId()).isEmpty()) {
                positionRepository.save(TrainPosition.builder()
                    .trainId(t.getId())
                    .latitude(t.getLatitude())
                    .longitude(t.getLongitude())
                    .speed(75.0)
                    .currentStation(t.getFromStation().getName())
                    .build());
            }
        }
    }

    private void seedFoodItems() {
        List<FoodItem> items = List.of(
            FoodItem.builder().name("Veg Thali").description("Complete vegetarian meal with rice, dal, sabzi, roti, salad").price(BigDecimal.valueOf(180)).category("LUNCH").vegetarian(true).available(true).build(),
            FoodItem.builder().name("Chicken Biryani").description("Aromatic basmati rice with tender chicken pieces").price(BigDecimal.valueOf(220)).category("LUNCH").vegetarian(false).available(true).build(),
            FoodItem.builder().name("Masala Dosa").description("Crispy dosa with potato filling and sambar").price(BigDecimal.valueOf(120)).category("BREAKFAST").vegetarian(true).available(true).build(),
            FoodItem.builder().name("Poha").description("Flattened rice with vegetables and spices").price(BigDecimal.valueOf(80)).category("BREAKFAST").vegetarian(true).available(true).build(),
            FoodItem.builder().name("Veg Sandwich").description("Grilled sandwich with vegetables and cheese").price(BigDecimal.valueOf(95)).category("SNACKS").vegetarian(true).available(true).build(),
            FoodItem.builder().name("Samosa (2 pcs)").description("Crispy pastry filled with spiced potatoes").price(BigDecimal.valueOf(60)).category("SNACKS").vegetarian(true).available(true).build(),
            FoodItem.builder().name("Tea").description("Hot masala chai").price(BigDecimal.valueOf(30)).category("BEVERAGES").vegetarian(true).available(true).build(),
            FoodItem.builder().name("Coffee").description("Hot brewed coffee").price(BigDecimal.valueOf(45)).category("BEVERAGES").vegetarian(true).available(true).build(),
            FoodItem.builder().name("Mineral Water (1L)").description("Packaged drinking water").price(BigDecimal.valueOf(25)).category("BEVERAGES").vegetarian(true).available(true).build(),
            FoodItem.builder().name("Paneer Tikka").description("Marinated cottage cheese grilled to perfection").price(BigDecimal.valueOf(160)).category("DINNER").vegetarian(true).available(true).build(),
            FoodItem.builder().name("Mutton Curry with Rice").description("Slow-cooked mutton curry served with steamed rice").price(BigDecimal.valueOf(260)).category("DINNER").vegetarian(false).available(true).build(),
            FoodItem.builder().name("Gulab Jamun (2 pcs)").description("Soft milk-solid dumplings in sugar syrup").price(BigDecimal.valueOf(70)).category("SNACKS").vegetarian(true).available(true).build()
        );
        foodItemRepository.saveAll(items);
    }
}
