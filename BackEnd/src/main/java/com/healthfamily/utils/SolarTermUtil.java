package com.healthfamily.utils;

import com.nlf.calendar.Lunar;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * 节气计算工具类
 * 使用 lunar-java 库进行精确计算
 */
public class SolarTermUtil {

    /**
     * 获取指定日期的最近节气（如果是当天则返回当天节气，否则返回上一个节气）
     * 返回"当前所处的节气时段"
     */
    public static String getCurrentSolarTerm() {
        return getSolarTerm(LocalDate.now());
    }

    public static String getSolarTerm(LocalDate date) {
        // 转换 LocalDate 为 java.util.Date
        Date utilDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        // 往前查找最近的一个节气（最多查找18天，因为节气间隔通常15-16天）
        for (int i = 0; i < 20; i++) {
            Lunar lunar = Lunar.fromDate(utilDate);
            String term = lunar.getJieQi();
            if (term != null && !term.isEmpty()) {
                return term;
            }
            // 前一天
            utilDate = new Date(utilDate.getTime() - 24 * 60 * 60 * 1000L);
        }
        
        // 兜底（理论上不会执行到这里）
        return "未知";
    }

    /**
     * 判断指定日期是否正好是节气交接日
     */
    public static boolean isSolarTermDate(LocalDate date) {
        Date utilDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Lunar lunar = Lunar.fromDate(utilDate);
        String term = lunar.getJieQi();
        return term != null && !term.isEmpty();
    }

    public static void main(String[] args) {
        System.out.println("2024-01-05: " + getSolarTerm(LocalDate.of(2024, 1, 5)));
        System.out.println("2024-01-06: " + getSolarTerm(LocalDate.of(2024, 1, 6)));
        System.out.println("2026-01-19: " + getSolarTerm(LocalDate.of(2026, 1, 19)));
        System.out.println("2026-01-20: " + getSolarTerm(LocalDate.of(2026, 1, 20)));
    }
}
