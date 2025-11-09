package com.travelplanner.dto;

public class VoicePlanRequest {
    private String voiceText;
    private Long userId;

    // 构造方法
    public VoicePlanRequest() {}

    public VoicePlanRequest(String voiceText, Long userId) {
        this.voiceText = voiceText;
        this.userId = userId;
    }

    // getters and setters
    public String getVoiceText() {
        return voiceText;
    }

    public void setVoiceText(String voiceText) {
        this.voiceText = voiceText;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}