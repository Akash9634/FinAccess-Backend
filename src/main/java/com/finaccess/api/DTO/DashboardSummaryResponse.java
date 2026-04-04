package com.finaccess.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardSummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private List<CategorySummary> categoryTotals;
    private List<MonthlyTrend> monthlyTrends;
    private List<RecordResponse> recentActivity;
}
