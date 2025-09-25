package com.example.banking.mapper;

import com.example.banking.dto.TransactionResponse;
import com.example.banking.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {

    // Domain -> DTO
    TransactionResponse toResponse(Transaction transaction);

    // DTO -> Domain
    Transaction toDomain(TransactionResponse response);
}