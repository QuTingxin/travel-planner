package com.travelplanner.controller;

import com.travelplanner.dto.VoicePlanRequest;
import com.travelplanner.entity.TravelPlan;
import com.travelplanner.entity.User;
import com.travelplanner.service.AIService;
import com.travelplanner.service.TravelPlanService;
import com.travelplanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/voice-plan")
@CrossOrigin(origins = "*")
public class VoicePlanController {

    @Autowired
    private AIService aiService;

    @Autowired
    private TravelPlanService travelPlanService;

    @Autowired
    private UserService userService;

    @PostMapping("/generate")
    public ResponseEntity<?> generatePlanFromVoice(@RequestBody VoicePlanRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 调用AI服务解析语音并生成行程
            Map<String, Object> aiResult = aiService.parseVoiceAndGeneratePlan(request.getVoiceText());

            // 创建旅行计划
            TravelPlan travelPlan = createTravelPlanFromAIData(aiResult, user);
            TravelPlan savedPlan = travelPlanService.createTravelPlan(travelPlan, user);

            // 返回结果
            Map<String, Object> response =  new HashMap<String, Object>(){{
                put("plan", savedPlan);
                put("aiAnalysis", aiResult);
            }};
//                    Map.of(
//                    "plan", savedPlan,
//                    "aiAnalysis", aiResult
//            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("生成旅行计划失败: " + e.getMessage());
        }
    }

    private TravelPlan createTravelPlanFromAIData(Map<String, Object> aiResult, User user) {
        Map<String, Object> parsedInfo = (Map<String, Object>) aiResult.get("parsedInfo");
        String itinerary = (String) aiResult.get("itinerary");

        TravelPlan plan = new TravelPlan();
        plan.setDestination((String) parsedInfo.get("destination"));
        plan.setStartDate(java.time.LocalDate.parse((String) parsedInfo.get("startDate")));
        plan.setEndDate(java.time.LocalDate.parse((String) parsedInfo.get("endDate")));
        plan.setBudget(((Number) parsedInfo.get("budget")).doubleValue());
        plan.setTravelerCount(((Number) parsedInfo.get("travelerCount")).intValue());

        // 将偏好数组转换为字符串
        Object preferences = parsedInfo.get("preferences");
        if (preferences instanceof java.util.List) {
            plan.setPreferences(String.join(",", (java.util.List<String>) preferences));
        } else {
            plan.setPreferences(preferences.toString());
        }

        plan.setItinerary(itinerary);
        plan.setUser(user);

        return plan;
    }
}