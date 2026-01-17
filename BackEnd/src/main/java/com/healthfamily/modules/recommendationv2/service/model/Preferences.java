package com.healthfamily.modules.recommendationv2.service.model;

import java.util.List;

public class Preferences {
  private List<String> liked;
  private List<String> disliked;
  public List<String> getLiked() { return liked; }
  public void setLiked(List<String> liked) { this.liked = liked; }
  public List<String> getDisliked() { return disliked; }
  public void setDisliked(List<String> disliked) { this.disliked = disliked; }
}
