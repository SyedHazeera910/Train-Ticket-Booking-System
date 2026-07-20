package com.railway.food.controller;

import com.railway.food.entity.FoodItem;
import com.railway.food.entity.FoodOrder;
import com.railway.food.repository.FoodItemRepository;
import com.railway.food.repository.FoodOrderRepository;
import com.railway.identity.entity.User;
import com.railway.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.railway.food.dto.FoodOrderRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
public class FoodController {

    private final FoodItemRepository foodItemRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final UserRepository userRepository;

    @GetMapping("/menu")
    public ResponseEntity<List<FoodItem>> getMenu() {
        return ResponseEntity.ok(foodItemRepository.findByAvailableTrue());
    }

    @PostMapping("/order")
    public ResponseEntity<FoodOrder> placeOrder(
            @Valid @RequestBody FoodOrderRequest request,
            Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        FoodOrder order = FoodOrder.builder()
                .user(user)
                .bookingId(request.getBookingId())
                .trainId(request.getTrainId())
                .itemsJson(request.getItemsJson())
                .total(request.getTotal())
                .status(FoodOrder.OrderStatus.PLACED)
                .build();

        return ResponseEntity.ok(foodOrderRepository.save(order));
    }

    @GetMapping("/orders/my")
    public ResponseEntity<List<FoodOrder>> getMyOrders(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(foodOrderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }
}
