package com.finaccess.api.repository;

import com.finaccess.api.model.FinancialRecord;
import com.finaccess.api.model.RecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RecordRepository extends JpaRepository<FinancialRecord, Long> {

    //never return soft deleted records
    List<FinancialRecord> findByDeletedFalse();

    //filters
    List<FinancialRecord> findByTypeAndDeletedFalse(RecordType type);
    List<FinancialRecord> findByCategoryIgnoreCaseAndDeletedFalse(String category);
    List<FinancialRecord> findByDateBetweenAndDeletedFalse(LocalDate from, LocalDate to);


    // dashboard queries
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.type = 'INCOME' AND r.deleted = false")
    BigDecimal sumTotalIncome();

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.type = 'EXPENSE' AND r.deleted = false")
    BigDecimal sumTotalExpenses();

    @Query("SELECT r.category, SUM(r.amount) FROM FinancialRecord r WHERE r.deleted = false GROUP BY r.category")
    List<Object[]> sumByCategory();

    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false ORDER BY r.createdAt DESC LIMIT 10")
    List<FinancialRecord> findRecentActivity();
}
