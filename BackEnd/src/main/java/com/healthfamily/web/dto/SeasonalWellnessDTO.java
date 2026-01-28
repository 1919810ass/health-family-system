package com.healthfamily.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeasonalWellnessDTO {
    private String solarTerm;
    private String constitution;
    private String advice;
    private String imageUrl;
}
