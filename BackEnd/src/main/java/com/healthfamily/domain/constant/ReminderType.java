package com.healthfamily.domain.constant;

/**
 * 健康提醒类型
 */
public enum ReminderType {
    MEDICATION,      // 用药提醒
    MEASUREMENT,     // 测量提醒（血压、血糖等）
    VACCINE,         // 疫苗接种提醒
    LIFESTYLE,       // 生活方式提醒（运动、饮食等）
    ABNORMAL,        // 异常数据提醒
    ROUTINE,         // 常规提醒
    FOLLOW_UP        // 随访跟踪
}

