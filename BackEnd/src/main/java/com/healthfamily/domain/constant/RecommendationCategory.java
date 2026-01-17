package com.healthfamily.domain.constant;

public enum RecommendationCategory {
    DIET("饮食", "primary"),
    REST("作息", "success"),
    SPORT("运动", "warning"),
    EMOTION("情绪", "danger"),
    VITALS("体征", "info"),
    LIFESTYLE("生活方式", "default");

    private final String displayName;
    private final String tagType;

    RecommendationCategory(String displayName, String tagType) {
        this.displayName = displayName;
        this.tagType = tagType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTagType() {
        return tagType;
    }
}

