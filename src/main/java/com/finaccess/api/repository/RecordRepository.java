package com.finaccess.api.repository;

import com.finaccess.api.model.FinancialRecord;
import com.finaccess.api.model.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecordRepository extends JpaRepository<FinancialRecord, Long> {

    //never return soft deleted records
    List<FinancialRecord> findByDeletedFalse();

    //filters
    List<FinancialRecord> findByTypeAndDeletedFalse(RecordType type);
    List<FinancialRecord> findByCategoryIgnoreCaseAndDeletedFalse(String category);
    List<FinancialRecord> findByDateBetweenAndDeletedFalse(LocalDate from, LocalDate to);

    //combined flexible filter
    @Query("""
    SELECT r FROM FinancialRecord r
    WHERE r.deleted = false
      AND (:type     IS NULL OR r.type = :type)
      AND (:category IS NULL OR LOWER(r.category) = LOWER(:category))
      AND (:from     IS NULL OR r.date >= :from)
      AND (:to       IS NULL OR r.date <= :to)
      AND (:keyword  IS NULL OR LOWER(r.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
                             OR LOWER(r.notes)    LIKE LOWER(CONCAT('%', :keyword, '%')))
    ORDER BY r.date DESC
""")
    Page<FinancialRecord> findWithFilters(
            @Param("type")     RecordType type,
            @Param("category") String category,
            @Param("from")     LocalDate from,
            @Param("to")       LocalDate to,
            @Param("keyword")  String keyword,
            Pageable pageable
    );

    @Query("""
    SELECT r FROM FinancialRecord r
    WHERE r.deleted = false
    ORDER BY r.date ASC
""")
    List<FinancialRecord> findAllForTrends();

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
