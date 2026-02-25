package com.healthfamily.modules.recommendationv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@Profile("recommendation-alone")
/**
 * Application
 * <p>
 * 属于业务子模块的核心组件，用于承载该模块的领域模型与服务逻辑。
 * </p>
 */
@SpringBootApplication
public class Application {
  /**
   * 执行业务操作
   * @param args 业务参数
   * @return 业务返回结果
   */
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
