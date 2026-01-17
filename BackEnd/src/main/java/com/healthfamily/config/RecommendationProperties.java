package com.healthfamily.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "recommendation")
public class RecommendationProperties {

    /**
     * 每个类别默认返回的最大项目数量。
     */
    private int maxItems = 6;

    private Ai ai = new Ai();

    @Getter
    @Setter
    public static class Ai {
        /**
         * Prompt 内容版本，用于埋点实体。
         */
        private String promptVersion = "v1";

        /**
         * 是否默认开启严格模式（对AI输出做更严格约束）。
         */
        private boolean strictModeDefault = false;

        /**
         * 每个类别允许的最大建议数量。
         */
        private int perCategoryLimit = 3;

        /**
         * 覆盖模型名，不配置则沿用 spring.ai.* 默认。
         */
        private String model;
    }
}


