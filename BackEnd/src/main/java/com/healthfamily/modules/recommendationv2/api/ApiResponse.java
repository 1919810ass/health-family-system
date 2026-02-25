package com.healthfamily.modules.recommendationv2.api;

/**
 * ApiResponse
 * <p>
 * 属于业务子模块的核心组件，用于承载该模块的领域模型与服务逻辑。
 * </p>
 */
import java.time.Instant;

public class ApiResponse<T> {
  private int code;
  private String message;
  private T data;
  private long timestamp;

  public ApiResponse() {}

  public ApiResponse(int code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
    this.timestamp = Instant.now().toEpochMilli();
  }

  /**

   * 执行业务操作

   * @param data 业务参数

   * @return 业务返回结果

   */

  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(0, "ok", data);
  }

  /**

   * 执行业务操作

   * @param code 业务参数

   * @param message 业务参数

   * @return 业务返回结果

   */

  public static <T> ApiResponse<T> error(int code, String message) {
    return new ApiResponse<>(code, message, null);
  }

  /**

   * 获取

   * @return 业务返回结果

   */

  public int getCode() { return code; }
  /**
   * 执行业务操作
   * @param code 业务参数
   * @return 无
   */
  public void setCode(int code) { this.code = code; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getMessage() { return message; }
  /**
   * 执行业务操作
   * @param message 业务参数
   * @return 无
   */
  public void setMessage(String message) { this.message = message; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public T getData() { return data; }
  /**
   * 执行业务操作
   * @param data 业务参数
   * @return 无
   */
  public void setData(T data) { this.data = data; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public long getTimestamp() { return timestamp; }
  /**
   * 执行业务操作
   * @param timestamp 业务参数
   * @return 无
   */
  public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
