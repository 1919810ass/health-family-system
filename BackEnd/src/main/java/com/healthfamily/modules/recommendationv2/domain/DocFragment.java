package com.healthfamily.modules.recommendationv2.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
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

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public byte[] getEmbedding() { return embedding; }
  public void setEmbedding(byte[] embedding) { this.embedding = embedding; }
  public String getTags() { return tags; }
  public void setTags(String tags) { this.tags = tags; }
  public String getSource() { return source; }
  public void setSource(String source) { this.source = source; }
  public String getVersion() { return version; }
  public void setVersion(String version) { this.version = version; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
