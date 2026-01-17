package com.healthfamily.modules.recommendationv2.service;

import com.healthfamily.modules.recommendationv2.domain.DocFragment;
import com.healthfamily.modules.recommendationv2.repository.DocFragmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
public class DocRagService {
  private final DocFragmentRepository repo;
  private final int topK;
  private final double minScore;

  public DocRagService(DocFragmentRepository repo,
                       @Value("${rag.topK}") int topK,
                       @Value("${rag.minScore}") double minScore) {
    this.repo = repo;
    this.topK = topK;
    this.minScore = minScore;
  }

  public List<Map<String,Object>> search(String q) {
    List<DocFragment> list = repo.findTop10ByTitleContainingIgnoreCase(q);
    List<Map<String,Object>> out = new ArrayList<>();
    for (DocFragment d : list) {
      double s = similarity(q, d.getContent());
      if (s < minScore) continue;
      Map<String,Object> m = new HashMap<>();
      m.put("title", d.getTitle());
      m.put("snippet", d.getContent().length() > 300 ? d.getContent().substring(0,300) : d.getContent());
      m.put("source", d.getSource());
      m.put("fragmentId", d.getId());
      m.put("score", s);
      out.add(m);
    }
    out.sort(Comparator.comparingDouble(o -> -((Double) o.get("score"))));
    if (out.size() > topK) return out.subList(0, topK);
    return out;
  }

  private double similarity(String a, String b) {
    byte[] va = hash(a);
    byte[] vb = hash(b);
    double dot = 0;
    double na = 0;
    double nb = 0;
    for (int i = 0; i < va.length; i++) {
      dot += (va[i] & 0xFF) * (vb[i] & 0xFF);
      na += (va[i] & 0xFF) * (va[i] & 0xFF);
      nb += (vb[i] & 0xFF) * (vb[i] & 0xFF);
    }
    if (na == 0 || nb == 0) return 0.0;
    return dot / (Math.sqrt(na) * Math.sqrt(nb));
  }

  private byte[] hash(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return md.digest(s.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      return new byte[32];
    }
  }
}
