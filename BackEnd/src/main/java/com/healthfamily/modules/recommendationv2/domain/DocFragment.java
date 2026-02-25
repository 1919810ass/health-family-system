package com.healthfamily.modules.recommendationv2.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
/**
 * DocFragment
 * <p>
 * 属于业务子模块的核心组件，用于承载该模块的领域模型与服务逻辑。
 * </p>
 */
@Table(name = "doc_fragments_v2")
public class DocFragment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  @Column(columnDefinition = "text")
  private String content;

  @Lob
  @Column(name = "embedding")
  private byte[] embedding;

  @Column(name = "tags", columnDefinition = "json")
  private String tags;

  private String source;

  private String version;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  /**

   * 获取

   * @return 业务返回结果

   */

  public Long getId() { return id; }
  /**
   * 执行业务操作
   * @param id 业务对象唯一标识
   * @return 无
   */
  public void setId(Long id) { this.id = id; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getTitle() { return title; }
  /**
   * 执行业务操作
   * @param title 业务参数
   * @return 无
   */
  public void setTitle(String title) { this.title = title; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getContent() { return content; }
  /**
   * 执行业务操作
   * @param content 业务参数
   * @return 无
   */
  public void setContent(String content) { this.content = content; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public byte[] getEmbedding() { return embedding; }
  /**
   * 执行业务操作
   * @param embedding 业务参数
   * @return 无
   */
  public void setEmbedding(byte[] embedding) { this.embedding = embedding; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getTags() { return tags; }
  /**
   * 执行业务操作
   * @param tags 业务参数
   * @return 无
   */
  public void setTags(String tags) { this.tags = tags; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getSource() { return source; }
  /**
   * 执行业务操作
   * @param source 业务参数
   * @return 无
   */
  public void setSource(String source) { this.source = source; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public String getVersion() { return version; }
  /**
   * 执行业务操作
   * @param version 业务参数
   * @return 无
   */
  public void setVersion(String version) { this.version = version; }
  /**
   * 获取
   * @return 业务返回结果
   */
  public Instant getCreatedAt() { return createdAt; }
  /**
   * 执行业务操作
   * @param createdAt 业务参数
   * @return 无
   */
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
