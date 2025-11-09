package com.travelplanner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.dto.AliyunAIRequest;
import com.travelplanner.dto.AliyunAIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.*;

/**
 * AI服务类 - 集成阿里云通义千问大模型
 */
@Service
public class AIService {
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    @Value("${aliyun.ai.api-key:sk-default}")
    private String aliYunApiKey;

    @Value("${aliyun.ai.endpoint:https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation}")
    private String aliYunEndpoint;

    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;

    public AIService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * 生成智能行程规划 - 主要方法
     */
    public String generateItinerary(String destination, String startDate, String endDate,
                                    Double budget, Integer travelerCount, String preferences) {
        try {
            // 构建详细的提示词
            String prompt = buildDetailedItineraryPrompt(destination, startDate, endDate, budget, travelerCount, preferences);

            // 调用阿里云大模型
            String aiResponse = callAliYunQwenModel(prompt);

            // 如果AI调用失败，返回模拟数据
            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                logger.warn("AI服务调用失败，返回模拟数据");
                return generateMockItinerary(destination, startDate, endDate, budget, travelerCount, preferences);
            }

            return aiResponse;

        } catch (Exception e) {
            logger.error("生成行程规划时发生错误", e);
            return generateMockItinerary(destination, startDate, endDate, budget, travelerCount, preferences);
        }
    }

    /**
     * 从语音文本解析旅行需求并生成行程规划
     */
    public Map<String, Object> parseVoiceAndGeneratePlan(String voiceText) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 解析语音文本获取结构化信息
            Map<String, Object> parsedInfo = parseTravelRequirements(voiceText);

            // 2. 生成详细行程
            String itinerary = generateItinerary(
                    (String) parsedInfo.get("destination"),
                    (String) parsedInfo.get("startDate"),
                    (String) parsedInfo.get("endDate"),
                    ((Number) parsedInfo.get("budget")).doubleValue(),
                    ((Number) parsedInfo.get("travelerCount")).intValue(),
                    String.join(",", (List<String>) parsedInfo.get("preferences"))
            );

            // 3. 构建返回结果
            result.put("parsedInfo", parsedInfo);
            result.put("itinerary", itinerary);
            result.put("summary", generateBudgetSummary(parsedInfo));

        } catch (Exception e) {
            logger.error("解析语音生成计划时发生错误", e);
            // 返回默认结果
            result.put("error", "生成计划失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 调用阿里云通义千问模型
     */
    private String callAliYunQwenModel(String prompt) {
        try {
            // 构建请求
            AliyunAIRequest request = new AliyunAIRequest(prompt);
            String requestBody = objectMapper.writeValueAsString(request);

            // 创建HTTP请求
            HttpPost httpPost = new HttpPost(aliYunEndpoint);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + aliYunApiKey);
            httpPost.setEntity(new StringEntity(requestBody, "UTF-8"));

            logger.info("调用阿里云AI服务，提示词长度: {}", prompt.length());

            // 执行请求
            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode == 200) {
                    AliyunAIResponse aiResponse = objectMapper.readValue(responseBody, AliyunAIResponse.class);
                    if (aiResponse.getOutput() != null && aiResponse.getOutput().getText() != null) {
                        String text = aiResponse.getOutput().getText();
                        logger.info("AI响应成功，长度: {}", text.length());
                        return text;
                    }
                }

                logger.error("阿里云AI服务调用失败，状态码: {}, 响应: {}", statusCode, responseBody);
                return null;
            });

        } catch (Exception e) {
            logger.error("调用阿里云AI服务时发生异常", e);
            return null;
        }
    }

    /**
     * 构建详细的行程规划提示词
     */
    private String buildDetailedItineraryPrompt(String destination, String startDate, String endDate,
                                                Double budget, Integer travelerCount, String preferences) {
        return String.format(
                "你是一个专业的旅行规划专家。请为以下旅行需求制定一份详细、实用的旅行计划。\n\n" +
                        "=== 旅行基本信息 ===\n" +
                        "📍 目的地：%s\n" +
                        "📅 旅行时间：%s 至 %s\n" +
                        "💰 总预算：%.2f元\n" +
                        "👥 旅行人数：%d人\n" +
                        "❤️ 旅行偏好：%s\n\n" +
                        "=== 请按照以下格式提供旅行计划 ===\n" +
                        "\n" +
                        "🌍 旅行概览\n" +
                        "• 目的地特色介绍\n" +
                        "• 最佳旅行季节说明\n" +
                        "• 行程天数安排\n" +
                        "\n" +
                        "🗓️ 每日详细行程安排\n" +
                        "请按天详细规划，每天包含：\n" +
                        "🏨 住宿建议（具体区域和酒店类型）\n" +
                        "🚗 交通安排（机场接送、市内交通）\n" +
                        "🏛️ 景点游览（具体景点、游览时间）\n" +
                        "🍽️ 餐饮推荐（早中晚餐具体推荐）\n" +
                        "🛍️ 购物建议（特色商品、购物地点）\n" +
                        "\n" +
                        "💰 详细预算分配\n" +
                        "请按以下类别详细分配预算：\n" +
                        "• 交通费用（往返机票、市内交通）\n" +
                        "• 住宿费用（酒店价格范围）\n" +
                        "• 餐饮费用（每日餐饮预算）\n" +
                        "• 景点门票\n" +
                        "• 购物娱乐\n" +
                        "• 应急备用金\n" +
                        "\n" +
                        "📝 实用贴士\n" +
                        "• 当地天气和着装建议\n" +
                        "• 必备物品清单\n" +
                        "• 文化习俗注意事项\n" +
                        "• 安全提示\n" +
                        "• 紧急联系方式\n" +
                        "\n" +
                        "💡 个性化建议\n" +
                        "根据旅行偏好 '%s' 提供特色推荐\n" +
                        "\n" +
                        "请确保：\n" +
                        "1. 行程安排合理，不过于紧凑\n" +
                        "2. 预算分配符合实际情况\n" +
                        "3. 提供具体的地点和时间建议\n" +
                        "4. 考虑交通便利性和时间效率\n" +
                        "5. 用中文回复，内容详实具体\n" +
                        "6. 使用emoji让内容更生动\n" +
                        "7. 总字数在1500字左右\n",
                destination, startDate, endDate, budget, travelerCount, preferences, preferences
        );
    }

    /**
     * 解析语音文本获取结构化信息
     */
    private Map<String, Object> parseTravelRequirements(String voiceText) {
        try {
            String prompt = buildParsePrompt(voiceText);
            String response = callAliYunQwenModel(prompt);

            if (response != null) {
                // 尝试从响应中提取JSON
                return extractJsonFromResponse(response);
            }

        } catch (Exception e) {
            logger.error("解析语音文本时发生错误", e);
        }

        // 如果解析失败，使用基于规则的解析
        return parseWithRuleBased(voiceText);
    }

    /**
     * 构建解析提示词
     */
    private String buildParsePrompt(String voiceText) {
        return String.format(
                "请从以下用户的语音输入中精确解析出旅行需求信息，并严格按照JSON格式返回。\n\n" +
                        "用户语音输入：%s\n\n" +
                        "需要解析的字段：\n" +
                        "{\n" +
                        "  \"destination\": \"目的地，如'日本东京'，如果没有明确目的地则返回'未知'\",\n" +
                        "  \"startDate\": \"开始日期，格式YYYY-MM-DD，如果没有明确日期则返回今天之后第7天的日期\",\n" +
                        "  \"endDate\": \"结束日期，格式YYYY-MM-DD，如果没有明确日期则返回开始日期后5天的日期\",\n" +
                        "  \"budget\": \"总预算数字，如果没有明确预算则根据目的地估算\",\n" +
                        "  \"travelerCount\": \"旅行人数，默认2人\",\n" +
                        "  \"preferences\": [\"旅行偏好数组，如'美食'、'文化'、'购物'等\"],\n" +
                        "  \"travelType\": \"旅行类型，如'家庭游'、'情侣游'、'朋友游'等\",\n" +
                        "  \"specialRequirements\": \"特殊需求\"\n" +
                        "}\n\n" +
                        "请直接返回JSON对象，不要有任何其他文字说明。",
                voiceText
        );
    }

    /**
     * 从响应中提取JSON
     */
    private Map<String, Object> extractJsonFromResponse(String response) {
        try {
            // 尝试找到JSON开始和结束位置
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}") + 1;

            if (start >= 0 && end > start) {
                String jsonStr = response.substring(start, end);
                return objectMapper.readValue(jsonStr, Map.class);
            }
        } catch (Exception e) {
            logger.warn("无法从响应中解析JSON，使用规则解析");
        }

        return parseWithRuleBased(response);
    }

    /**
     * 基于规则的解析（备用方案）
     */
    private Map<String, Object> parseWithRuleBased(String text) {
        Map<String, Object> result = new HashMap<>();

        // 设置默认值
        result.put("destination", extractDestination(text));
        result.put("startDate", getDefaultStartDate());
        result.put("endDate", getDefaultEndDate());
        result.put("budget", extractBudget(text));
        result.put("travelerCount", extractTravelerCount(text));
        result.put("preferences", extractPreferences(text));
        result.put("travelType", extractTravelType(text));
        result.put("specialRequirements", extractSpecialRequirements(text));

        return result;
    }

    // 基于规则的解析辅助方法
    private String extractDestination(String text) {
        if (text.contains("日本") || text.contains("东京")) return "日本东京";
        if (text.contains("三亚")) return "海南三亚";
        if (text.contains("成都")) return "四川成都";
        if (text.contains("上海")) return "上海";
        if (text.contains("北京")) return "北京";
        return "未知目的地";
    }

    private String getDefaultStartDate() {
        return java.time.LocalDate.now().plusDays(7).toString();
    }

    private String getDefaultEndDate() {
        return java.time.LocalDate.now().plusDays(12).toString();
    }

    private Double extractBudget(String text) {
        if (text.matches(".*[1-9]万.*")) {
            return 10000.0;
        }
        if (text.matches(".*[2-5]千.*")) {
            return 3000.0;
        }
        return 5000.0; // 默认预算
    }

    private Integer extractTravelerCount(String text) {
        if (text.contains("一家") || text.contains("带孩子")) return 3;
        if (text.contains("两个") || text.contains("两人")) return 2;
        if (text.contains("一个") || text.contains("独自")) return 1;
        return 2; // 默认2人
    }

    private List<String> extractPreferences(String text) {
        List<String> preferences = new ArrayList<>();
        if (text.contains("美食") || text.contains("吃")) preferences.add("美食");
        if (text.contains("动漫")) preferences.add("动漫");
        if (text.contains("购物")) preferences.add("购物");
        if (text.contains("文化")) preferences.add("文化");
        if (text.contains("海滩") || text.contains("海边")) preferences.add("海滩");
        if (preferences.isEmpty()) preferences.add("观光");
        return preferences;
    }

    private String extractTravelType(String text) {
        if (text.contains("家庭") || text.contains("带孩子")) return "家庭游";
        if (text.contains("情侣")) return "情侣游";
        if (text.contains("朋友")) return "朋友游";
        if (text.contains("独自")) return "个人游";
        return "休闲游";
    }

    private String extractSpecialRequirements(String text) {
        if (text.contains("带孩子")) return "需要儿童友好设施";
        if (text.contains("老人")) return "需要无障碍设施";
        return "无特殊需求";
    }

    /**
     * 生成预算摘要
     */
    private Map<String, Object> generateBudgetSummary(Map<String, Object> parsedInfo) {
        double budget = ((Number) parsedInfo.get("budget")).doubleValue();
        int travelers = ((Number) parsedInfo.get("travelerCount")).intValue();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalBudget", budget);
        summary.put("dailyBudget", budget / 5); // 默认5天
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
     * 生成模拟行程（当AI服务不可用时使用）
     */
    private String generateMockItinerary(String destination, String startDate, String endDate,
                                         Double budget, Integer travelerCount, String preferences) {
        return String.format(
                "🌍 %s 智能旅行规划\n\n" +
                        "📅 行程概览\n" +
                        "• 目的地：%s\n" +
                        "• 旅行时间：%s 至 %s（共%d天）\n" +
                        "• 总预算：¥%.2f元（人均¥%.2f）\n" +
                        "• 旅行人数：%d人\n" +
                        "• 旅行偏好：%s\n\n" +
                        "🗓️ 每日详细行程安排\n\n" +
                        "第一天：抵达%s\n" +
                        "🏨 住宿：市中心酒店，方便出行\n" +
                        "🚗 交通：机场专车接送\n" +
                        "🏛️ 景点：市区地标游览\n" +
                        "🍽️ 餐饮：当地特色餐厅\n\n" +
                        "第二天：深度探索\n" +
                        "🏨 住宿：同第一天酒店\n" +
                        "🚗 交通：地铁+出租车\n" +
                        "🏛️ 景点：主要景点参观\n" +
                        "🍽️ 餐饮：特色美食体验\n\n" +
                        "第三天：特色体验\n" +
                        "🏨 住宿：同第一天酒店\n" +
                        "🚗 交通：包车服务\n" +
                        "🏛️ 景点：根据偏好安排活动\n" +
                        "🍽️ 餐饮：网红餐厅打卡\n\n" +
                        "第四天：自由活动\n" +
                        "🏨 住宿：同第一天酒店\n" +
                        "🚗 交通：自由安排\n" +
                        "🏛️ 景点：购物或休闲\n" +
                        "🍽️ 餐饮：自选美食\n\n" +
                        "第五天：返程准备\n" +
                        "🏨 住宿：无（返程）\n" +
                        "🚗 交通：机场送机\n" +
                        "🏛️ 景点：周边最后游览\n" +
                        "🍽️ 餐饮：机场简餐\n\n" +
                        "💰 详细预算分配\n" +
                        "• 交通费用：30%% (¥%.2f)\n" +
                        "• 住宿费用：35%% (¥%.2f)\n" +
                        "• 餐饮费用：20%% (¥%.2f)\n" +
                        "• 景点门票：10%% (¥%.2f)\n" +
                        "• 购物娱乐：5%% (¥%.2f)\n\n" +
                        "📝 实用贴士\n" +
                        "• 建议提前预订机票和酒店\n" +
                        "• 准备当地货币和信用卡\n" +
                        "• 下载当地交通和翻译APP\n" +
                        "• 注意天气变化，准备合适衣物\n" +
                        "• 保持重要证件和财物安全\n\n" +
                        "💡 温馨提示\n" +
                        "此行程为AI智能生成，请根据实际情况调整。祝您旅途愉快！🎉",
                destination, destination, startDate, endDate, 5, budget, budget/travelerCount,
                travelerCount, preferences, destination,
                budget * 0.3, budget * 0.35, budget * 0.2, budget * 0.1, budget * 0.05
        );
    }
}