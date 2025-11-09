package com.travelplanner.service;

import com.travelplanner.entity.TravelPlan;
import com.travelplanner.entity.User;
import com.travelplanner.repository.TravelPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TravelPlanService {

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    @Autowired
    private AIService aiService;

    public List<TravelPlan> getUserTravelPlans(Long userId) {
        return travelPlanRepository.findByUserId(userId);
    }

    public TravelPlan createTravelPlan(TravelPlan travelPlan, User user) {
        travelPlan.setUser(user);

        // 调用AI服务生成行程
        String itinerary = aiService.generateItinerary(
                travelPlan.getDestination(),
                travelPlan.getStartDate().toString(),
                travelPlan.getEndDate().toString(),
                travelPlan.getBudget(),
                travelPlan.getTravelerCount(),
                travelPlan.getPreferences()
        );

        travelPlan.setItinerary(itinerary);
        return travelPlanRepository.save(travelPlan);
    }

    public TravelPlan getTravelPlan(Long planId) {
        return travelPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("旅行计划不存在"));
    }

    public void deleteTravelPlan(Long planId) {
        travelPlanRepository.deleteById(planId);
    }

    public List<TravelPlan> searchTravelPlans(Long userId, String destination) {
        if (destination != null && !destination.trim().isEmpty()) {
            return travelPlanRepository.findByDestinationContainingIgnoreCase(destination);
        }
        return getUserTravelPlans(userId);
    }
}