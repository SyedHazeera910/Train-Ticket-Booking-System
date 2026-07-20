package com.railway.food.repository;

import com.railway.food.entity.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    List<FoodOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
}
