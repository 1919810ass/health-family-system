package com.healthfamily.web.model.response;

import java.util.List;

/**
 * AI建议的响应模型
 */
public class AiSuggestionResponse {

    /**
     * 建议类型 (e.g., "POPULAR_REMINDERS", "HIGH_ENGAGEMENT")
     */
    private String suggestionType;

    /**
     * 建议标题 (e.g., "热门提醒分析")
     */
    private String title;

    /**
     * 建议的描述
     */
    private String description;

    /**
     * 建议的具体条目列表
     */
    private List<SuggestionItem> items;

    // Getters and Setters
    public String getSuggestionType() {
        return suggestionType;
    }

    public void setSuggestionType(String suggestionType) {
        this.suggestionType = suggestionType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SuggestionItem> getItems() {
        return items;
    }

    public void setItems(List<SuggestionItem> items) {
        this.items = items;
    }

    /**
     * 建议条目的内部类
     */
    public static class SuggestionItem {
        /**
         * 条目名称 (e.g., 提醒内容的文本)
         */
        private String name;
        /**
         * 条目值 (e.g., "设置次数: 500" or "完成率: 90%")
         */
        private String value;
        /**
         * 备注 (e.g., "相比上周+10%")
         */
        private String remark;

        public SuggestionItem(String name, String value, String remark) {
            this.name = name;
            this.value = value;
            this.remark = remark;
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
