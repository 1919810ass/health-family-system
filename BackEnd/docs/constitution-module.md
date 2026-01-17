# 体质数据处理模块

## 概述
- 目标：将体质测评问卷结果整合为结构化特征，参与饮食建议的生成与个性化权重调整。

## 组件
- 解析器与标准化：`modules/recommendationv2/service/ConstitutionProcessor`
- 特征模型：`modules/recommendationv2/service/model/ConstitutionFeatures`
- 服务集成：`modules/recommendationv2/service/RecommendationService`

## 输入
- 来源：`constitution_assessments.score_vector` JSON，示例：
```json
{"balanced":80,"qi_deficiency":60,"phlegm_damp":70}
```

## 输出
- 归一化到 [0,1] 的特征（九维），含主要体质与趋势：
```json
{
  "balanced":0.8,
  "qiDeficiency":0.6,
  "phlegmDamp":0.7,
  "primaryType":"qi_deficiency",
  "trend":{"qi_deficiency":0.03}
}
```

## 行为
- 缓存：用户级 TTL 10 分钟
- 个性化融合：
  - 气虚提高蛋白目标（最多 +0.2 系数）
  - 痰湿/湿热降低食用油目标（降至 20–25g/日）
  - `source_tags` 增加 `constitution:*` 与 `importance:*` 标签

## 接口影响
- V2 生成接口：`/api/recommendations/v2/generate` 返回首条结构化饮食建议中包含体质相关标签与分量调整

## 测试
- 单元：`ConstitutionProcessorTest` 验证解析与归一化
- 集成：建议补充饮食生成中标签与分量断言

## 性能
- 解析与趋势计算时间微小；缓存生效后不显著影响响应

## 风险与回滚
- 纯增量，不改变数据库结构；若需回滚，删除 `ConstitutionProcessor` 集成调用并恢复原 `buildDietItem`