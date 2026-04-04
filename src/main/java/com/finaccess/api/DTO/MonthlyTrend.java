package com.finaccess.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class MonthlyTrend {
    private String month;        //like - "2024-03"
    private BigDecimal income;
    private BigDecimal expenses;
}
