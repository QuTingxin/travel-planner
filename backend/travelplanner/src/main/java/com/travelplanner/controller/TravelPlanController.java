package com.travelplanner.controller;

import com.travelplanner.entity.TravelPlan;
import com.travelplanner.entity.User;
import com.travelplanner.service.TravelPlanService;
import com.travelplanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/travel-plans")
@CrossOrigin(origins = "*")
public class TravelPlanController {

    @Autowired
    private TravelPlanService travelPlanService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<TravelPlan>> getUserTravelPlans() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<TravelPlan> plans = travelPlanService.getUserTravelPlans(user.getId());
        return ResponseEntity.ok(plans);
    }

    @PostMapping
    public ResponseEntity<TravelPlan> createTravelPlan(@RequestBody TravelPlan travelPlan) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        TravelPlan createdPlan = travelPlanService.createTravelPlan(travelPlan, user); // qu
        return ResponseEntity.ok(createdPlan);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelPlan> getTravelPlan(@PathVariable Long id) {
        TravelPlan plan = travelPlanService.getTravelPlan(id);
        return ResponseEntity.ok(plan);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTravelPlan(@PathVariable Long id) {
        travelPlanService.deleteTravelPlan(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<TravelPlan>> searchTravelPlans(@RequestParam(required = false) String destination) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<TravelPlan> plans = travelPlanService.searchTravelPlans(user.getId(), destination);
        return ResponseEntity.ok(plans);
    }
}