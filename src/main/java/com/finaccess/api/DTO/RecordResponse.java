package com.finaccess.api.DTO;

import com.finaccess.api.model.FinancialRecord;
import com.finaccess.api.model.RecordType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecordResponse {
    private Long id;
    private BigDecimal amount;
    private RecordType type;
    private String category;
    private LocalDate date;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;

    // factory method — convert entity to DTO in one place
    public static RecordResponse from(FinancialRecord r) {
        RecordResponse dto = new RecordResponse();
        dto.id = r.getId();
        dto.amount = r.getAmount();
        dto.type = r.getType();
        dto.category = r.getCategory();
        dto.date = r.getDate();
        dto.notes = r.getNotes();
        dto.createdBy = r.getCreatedBy().getUsername();
        dto.createdAt = r.getCreatedAt();
        return dto;
    }
}
