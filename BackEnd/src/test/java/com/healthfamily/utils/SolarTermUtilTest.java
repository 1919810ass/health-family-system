package com.healthfamily.utils;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class SolarTermUtilTest {

    @Test
    public void testIsSolarTermDate() {
        // 2024-02-04 is Beginning of Spring (Li Chun)
        LocalDate liChun = LocalDate.of(2024, 2, 4);
        assertTrue(SolarTermUtil.isSolarTermDate(liChun), "2024-02-04 should be a solar term date");
        
        // getSolarTerm should return "立春"
        assertEquals("立春", SolarTermUtil.getSolarTerm(liChun));

        // 2024-02-05 is NOT a solar term date
        LocalDate dayAfter = LocalDate.of(2024, 2, 5);
        assertFalse(SolarTermUtil.isSolarTermDate(dayAfter), "2024-02-05 should NOT be a solar term date");
        
        // getSolarTerm should still return "立春" (as it is within the period)
        assertEquals("立春", SolarTermUtil.getSolarTerm(dayAfter));
    }
}
