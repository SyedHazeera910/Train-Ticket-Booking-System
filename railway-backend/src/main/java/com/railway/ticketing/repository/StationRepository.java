package com.railway.ticketing.repository;

import com.railway.ticketing.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByCode(String code);
    List<Station> findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(String name, String city);
}
