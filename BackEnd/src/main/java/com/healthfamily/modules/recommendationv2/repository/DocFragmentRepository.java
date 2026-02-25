package com.healthfamily.modules.recommendationv2.repository;

import com.healthfamily.modules.recommendationv2.domain.DocFragment;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * DocFragment数据访问接口
 * <p>
 * 属于业务子模块的核心组件，用于承载该模块的领域模型与服务逻辑。
 * </p>
 */
import java.util.List;

public interface DocFragmentRepository extends JpaRepository<DocFragment, Long> {
  List<DocFragment> findTop10ByTitleContainingIgnoreCase(String q);
}
