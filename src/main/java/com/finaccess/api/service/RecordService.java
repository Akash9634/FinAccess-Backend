package com.finaccess.api.service;

import com.finaccess.api.DTO.RecordRequest;
import com.finaccess.api.DTO.RecordResponse;
import com.finaccess.api.model.FinancialRecord;
import com.finaccess.api.model.RecordType;
import com.finaccess.api.model.User;
import com.finaccess.api.repository.RecordRepository;
import com.finaccess.api.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

public class RecordService {

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;

    public RecordService(RecordRepository recordRepository, UserRepository userRepository){
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
    }

    //get currently logged in user
    private User getCurrentUser(){
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    // create record — admin only
    public RecordResponse createRecord(RecordRequest request) {
        User currentUser = getCurrentUser();

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(currentUser)
                .build();

        return RecordResponse.from(recordRepository.save(record));
    }

    // get all records with optional filters — viewer, analyst, admin
    public List<RecordResponse> getRecords(RecordType type,
                                           String category,
                                           LocalDate from,
                                           LocalDate to) {
        return recordRepository.findWithFilters(type, category, from, to)
                .stream()
                .map(RecordResponse::from)
                .toList();
    }

    // get single record — all roles
    public RecordResponse getRecordById(Long id) {
        return RecordResponse.from(findActiveRecordById(id));
    }

    // update — admin only
    public RecordResponse updateRecord(Long id, RecordRequest request) {
        FinancialRecord record = findActiveRecordById(id);

        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setDate(request.getDate());
        record.setNotes(request.getNotes());

        return RecordResponse.from(recordRepository.save(record));
    }

    // soft delete — admin only
    public void deleteRecord(Long id) {
        FinancialRecord record = findActiveRecordById(id);
        record.setDeleted(true);
        recordRepository.save(record);
    }

    // private helper — checks soft delete
    private FinancialRecord findActiveRecordById(Long id) {
        return recordRepository.findById(id)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new RuntimeException("Record not found with id: " + id));
    }
}
