package com.finaccess.api.service;

import com.finaccess.api.DTO.CategorySummary;
import com.finaccess.api.DTO.DashboardSummaryResponse;
import com.finaccess.api.DTO.MonthlyTrend;
import com.finaccess.api.DTO.RecordResponse;
import com.finaccess.api.model.FinancialRecord;
import com.finaccess.api.model.RecordType;
import com.finaccess.api.repository.RecordRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class DashboardService {
    private final RecordRepository recordRepository;

    public DashboardService(RecordRepository recordRepository){
        this.recordRepository = recordRepository;
    }

    public DashboardSummaryResponse getSummary() {

        BigDecimal totalIncome   = recordRepository.sumTotalIncome();
        BigDecimal totalExpenses = recordRepository.sumTotalExpenses();
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);

        List<CategorySummary> categoryTotals = recordRepository.sumByCategory()
                .stream()
                .map(row -> new CategorySummary(
                        (String) row[0],
                        (BigDecimal) row[1]))
                .toList();

        List<MonthlyTrend> monthlyTrends = calculateMonthlyTrends();

        List<RecordResponse> recentActivity = recordRepository.findRecentActivity()
                .stream()
                .map(RecordResponse::from)
                .toList();

        return new DashboardSummaryResponse(
                totalIncome,
                totalExpenses,
                netBalance,
                categoryTotals,
                monthlyTrends,
                recentActivity
        );
    }

    private List<MonthlyTrend> calculateMonthlyTrends() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        Map<String, List<FinancialRecord>> groupedByMonth = recordRepository
                .findAllForTrends()
                .stream()
                .collect(Collectors.groupingBy(r -> r.getDate().format(formatter)));

        return groupedByMonth.entrySet().stream()
                .map(entry -> {
                    String month = entry.getKey();
                    List<FinancialRecord> records = entry.getValue();

                    BigDecimal income = records.stream()
                            .filter(r -> r.getType() == RecordType.INCOME)
                            .map(FinancialRecord::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal expenses = records.stream()
                            .filter(r -> r.getType() == RecordType.EXPENSE)
                            .map(FinancialRecord::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new MonthlyTrend(month, income, expenses);
                })
                .sorted(Comparator.comparing(MonthlyTrend::getMonth))
                .toList();
    }

}
