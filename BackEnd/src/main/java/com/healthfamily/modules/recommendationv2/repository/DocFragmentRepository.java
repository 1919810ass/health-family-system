package com.healthfamily.modules.recommendationv2.repository;

import com.healthfamily.modules.recommendationv2.domain.DocFragment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocFragmentRepository extends JpaRepository<DocFragment, Long> {
  List<DocFragment> findTop10ByTitleContainingIgnoreCase(String q);
}
