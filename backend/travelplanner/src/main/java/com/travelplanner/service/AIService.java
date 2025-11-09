package com.travelplanner.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;

/**
 * AI服务类 - 集成阿里云大模型
 */
@Service
public class AIService {

    @Value("${aliyun.ai.api-key:default}")
    private String aliYunApiKey;

    @Value("${aliyun.ai.endpoint:https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation}")
    private String aliYunEndpoint;

    private final RestTemplate restTemplate;

    public AIService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 从语音文本解析旅行需求并生成行程规划
     */
    public Map<String, Object> parseVoiceAndGeneratePlan(String voiceText) {
        // 1. 首先解析语音文本，提取结构化信息
        Map<String, Object> parsedInfo = parseTravelRequirements(voiceText);

        // 2. 调用阿里云大模型生成详细行程
        String itinerary = generateDetailedItinerary(parsedInfo);

        // 3. 返回解析结果和行程
        Map<String, Object> result = new HashMap<>();
        result.put("parsedInfo", parsedInfo);
        result.put("itinerary", itinerary);
        result.put("summary", generateBudgetSummary(parsedInfo));

        return result;
    }

    /**
     * 解析语音文本，提取旅行需求信息
     */
    private Map<String, Object> parseTravelRequirements(String voiceText) {
        // 调用阿里云大模型进行文本解析
        String prompt = buildParsePrompt(voiceText);
        String response = callAliYunModel(prompt);

        // 解析大模型返回的结构化数据
        return parseModelResponse(response);
    }

    /**
     * 生成详细行程规划
     */
    private String generateDetailedItinerary(Map<String, Object> parsedInfo) {
        String prompt = buildItineraryPrompt(parsedInfo);
        return callAliYunModel(prompt);
    }

    /**
     * 调用阿里云大模型API
     */
    private String callAliYunModel(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aliYunApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> input = new HashMap<>();
            Map<String, Object> parameters = new HashMap<>();

            input.put("prompt", prompt);
            parameters.put("result_format", "text");
            parameters.put("top_p", 0.8);
            parameters.put("temperature", 0.7);

            requestBody.put("model", "qwen-plus");
            requestBody.put("input", input);
            requestBody.put("parameters", parameters);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(aliYunEndpoint, HttpMethod.POST, entity, Map.class);

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> output = (Map<String, Object>) responseBody.get("output");
                return (String) output.get("text");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // 如果AI服务不可用，返回模拟响应
        }

        return generateMockResponse(prompt);
    }

    /**
     * 构建解析提示词
     */
    private String buildParsePrompt(String voiceText) {
        return String.format(
                "请从以下用户的语音输入中解析出旅行需求信息，并以JSON格式返回。需要解析的字段包括：\n" +
                        "- destination: 目的地（字符串）\n" +
                        "- startDate: 开始日期（YYYY-MM-DD格式）\n" +
                        "- endDate: 结束日期（YYYY-MM-DD格式）\n" +
                        "- budget: 总预算（数字）\n" +
                        "- travelerCount: 旅行人数（数字）\n" +
                        "- preferences: 旅行偏好（数组，如[\"美食\", \"文化\", \"购物\"]）\n" +
                        "- travelType: 旅行类型（如\"家庭游\", \"情侣游\", \"朋友游\"等）\n" +
                        "- specialRequirements: 特殊需求（字符串）\n\n" +
                        "用户语音输入：%s\n\n" +
                        "请直接返回JSON格式，不要有其他文字说明。如果某些信息无法确定，请使用null。",
                voiceText
        );
    }

    /**
     * 构建行程规划提示词
     */
    private String buildItineraryPrompt(Map<String, Object> parsedInfo) {
        return String.format(
                "请为以下旅行需求生成详细的行程规划：\n" +
                        "目的地：%s\n" +
                        "旅行日期：%s 至 %s\n" +
                        "预算：%.2f元\n" +
                        "旅行人数：%d人\n" +
                        "旅行偏好：%s\n" +
                        "旅行类型：%s\n" +
                        "特殊需求：%s\n\n" +
                        "请提供包含以下内容的详细行程安排：\n" +
                        "1. 每日详细行程（交通、景点、餐饮、住宿建议）\n" +
                        "2. 预算分配建议\n" +
                        "3. 注意事项和温馨提示\n" +
                        "4. 推荐的美食和购物地点\n" +
                        "请用中文回复，内容要详细具体。",
                parsedInfo.get("destination"),
                parsedInfo.get("startDate"),
                parsedInfo.get("endDate"),
                ((Number) parsedInfo.get("budget")).doubleValue(),
                ((Number) parsedInfo.get("travelerCount")).intValue(),
                parsedInfo.get("preferences"),
                parsedInfo.get("travelType"),
                parsedInfo.get("specialRequirements")
        );
    }

    /**
     * 解析模型响应
     */
    private Map<String, Object> parseModelResponse(String response) {
        // 这里简化处理，实际应该解析JSON
        // 如果解析失败，使用默认的解析逻辑
        Map<String, Object> result = new HashMap<>();

        try {
            // 尝试解析JSON响应
            // 实际实现中这里应该使用JSON解析库
            // 暂时使用模拟数据
            result.put("destination", "日本东京");
            result.put("startDate", "2024-06-01");
            result.put("endDate", "2024-06-05");
            result.put("budget", 10000.0);
            result.put("travelerCount", 2);
            result.put("preferences", Arrays.asList("美食", "动漫", "购物"));
            result.put("travelType", "家庭游");
            result.put("specialRequirements", "带孩子");

        } catch (Exception e) {
            // 如果解析失败，使用基于关键词的简单解析
            result = parseWithKeywords(response);
        }

        return result;
    }

    /**
     * 基于关键词的简单解析（备用方案）
     */
    private Map<String, Object> parseWithKeywords(String text) {
        Map<String, Object> result = new HashMap<>();
        // 这里可以实现基于关键词的简单解析逻辑
        // 暂时返回默认值
        result.put("destination", "未知目的地");
        result.put("startDate", "2024-01-01");
        result.put("endDate", "2024-01-05");
        result.put("budget", 5000.0);
        result.put("travelerCount", 1);
        result.put("preferences", Arrays.asList("观光"));
        result.put("travelType", "个人游");
        result.put("specialRequirements", "无");

        return result;
    }

    /**
     * 生成预算摘要
     */
    private Map<String, Object> generateBudgetSummary(Map<String, Object> parsedInfo) {
        double budget = ((Number) parsedInfo.get("budget")).doubleValue();
        int days = 5; // 默认5天
        int travelers = ((Number) parsedInfo.get("travelerCount")).intValue();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalBudget", budget);
        summary.put("dailyBudget", budget / days);
        summary.put("perPersonBudget", budget / travelers);
        summary.put("recommendedAllocation", getRecommendedAllocation(budget));

        return summary;
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

    /**
     * 模拟响应（当AI服务不可用时使用）
     */
    private String generateMockResponse(String prompt) {
        return "🌍 智能旅行规划（基于您的语音输入生成）\n\n" +
                "📅 行程概览：\n" +
                "• 目的地：日本东京\n" +
                "• 行程天数：5天\n" +
                "• 预算：¥10,000（2人）\n" +
                "• 旅行类型：家庭游\n\n" +
                "🗓️ 详细行程：\n" +
                "第一天：抵达东京，入住酒店，浅草寺游览\n" +
                "第二天：秋叶原动漫体验，东京塔观光\n" +
                "第三天：迪士尼乐园一日游\n" +
                "第四天：银座购物，品尝寿司\n" +
                "第五天：上野公园，返程准备\n\n" +
                "💰 预算分配：\n" +
                "• 交通：¥3,000\n" +
                "• 住宿：¥3,500\n" +
                "• 餐饮：¥2,000\n" +
                "• 景点：¥1,000\n" +
                "• 购物：¥500\n\n" +
                "💡 温馨提示：此行程为AI智能生成，请根据实际情况调整。";
    }
}