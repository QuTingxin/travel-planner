package com.travelplanner.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;

/**
 * AI服务类 - 模拟AI行程规划
 * 实际项目中可以接入OpenAI API或其他AI服务
 */
@Service
public class AIService {

    @Value("${ai.api.key:default}")
    private String aiApiKey;

    @Value("${ai.api.url:https://api.openai.com/v1/chat/completions}")
    private String aiApiUrl;

    private final RestTemplate restTemplate;

    public AIService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 生成智能行程规划
     */
    public String generateItinerary(String destination, String startDate, String endDate,
                                    Double budget, Integer travelerCount, String preferences) {

        // 模拟AI响应 - 实际项目中应调用真实的AI API
        String mockResponse = generateMockItinerary(destination, startDate, endDate, budget, travelerCount, preferences);

        // 实际调用AI API的代码（注释状态）
        /*
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个专业的旅行规划助手，请为用户生成详细的旅行行程。");
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", buildPrompt(destination, startDate, endDate, budget, travelerCount, preferences));
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 2000);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(aiApiUrl, HttpMethod.POST, entity, Map.class);

            // 解析响应...

        } catch (Exception e) {
            // 如果AI服务不可用，返回模拟响应
            return mockResponse;
        }
        */

        return mockResponse;
    }

    /**
     * 构建AI提示词
     */
    private String buildPrompt(String destination, String startDate, String endDate,
                               Double budget, Integer travelerCount, String preferences) {
        return String.format(
                "请为以下旅行需求生成详细的行程规划：\n" +
                        "目的地：%s\n" +
                        "旅行日期：%s 至 %s\n" +
                        "预算：%.2f元\n" +
                        "旅行人数：%d人\n" +
                        "旅行偏好：%s\n\n" +
                        "请提供包含交通、住宿、景点、餐厅等详细信息的行程安排，并确保在预算范围内。",
                destination, startDate, endDate, budget, travelerCount, preferences
        );
    }

    /**
     * 生成模拟行程规划（用于演示）
     */
    private String generateMockItinerary(String destination, String startDate, String endDate,
                                         Double budget, Integer travelerCount, String preferences) {
        return String.format(
                "🌍 %s %s-%s 旅行规划（预算：¥%.2f，%d人）\n\n" +
                        "📅 行程概览：\n" +
                        "• 总天数：%s\n" +
                        "• 人均预算：¥%.2f\n" +
                        "• 旅行偏好：%s\n\n" +
                        "🗓️ 详细行程：\n" +
                        "第一天：抵达%s，入住酒店，市区游览\n" +
                        "第二天：主要景点参观，体验当地美食\n" +
                        "第三天：根据偏好安排特色活动\n" +
                        "第四天：自由活动或深度探索\n" +
                        "第五天：购物纪念，返程准备\n\n" +
                        "💰 预算分配：\n" +
                        "• 交通：30%%\n" +
                        "• 住宿：35%%\n" +
                        "• 餐饮：20%%\n" +
                        "• 景点门票：10%%\n" +
                        "• 其他：5%%\n\n" +
                        "💡 温馨提示：此行程为AI智能生成，请根据实际情况调整。",
                destination, startDate, endDate, budget, travelerCount,
                "5天", budget/travelerCount, preferences, destination
        );
    }

    /**
     * 预算分析
     */
    public Map<String, Object> analyzeBudget(Double totalBudget, Integer days, Integer travelerCount) {
        Map<String, Object> analysis = new HashMap<>();

        double dailyBudget = totalBudget / days;
        double perPersonBudget = totalBudget / travelerCount;

        analysis.put("totalBudget", totalBudget);
        analysis.put("dailyBudget", dailyBudget);
        analysis.put("perPersonBudget", perPersonBudget);
        analysis.put("recommendedAllocation", getRecommendedAllocation(totalBudget));

        return analysis;
    }

    private Map<String, Double> getRecommendedAllocation(Double totalBudget) {
        Map<String, Double> allocation = new HashMap<>();
        allocation.put("transportation", totalBudget * 0.3);
        allocation.put("accommodation", totalBudget * 0.35);
        allocation.put("food", totalBudget * 0.2);
        allocation.put("attractions", totalBudget * 0.1);
        allocation.put("shopping", totalBudget * 0.05);
        return allocation;
    }
}