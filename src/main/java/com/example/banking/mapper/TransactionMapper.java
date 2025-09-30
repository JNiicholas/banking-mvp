package com.example.banking.mapper;

import com.example.banking.dto.TransactionResponse;
import com.example.banking.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {

    //TODO: Remove explicit mapping
    @Mapping(target = "id", source = "id")
    @Mapping(target = "timestamp", source = "timestamp")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "balanceAfter", source = "balanceAfter")
    TransactionResponse toResponse(Transaction transaction);

    //TODO: Remove explicit mapping
    @Mapping(target = "id", source = "id")
    @Mapping(target = "timestamp", source = "timestamp")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "balanceAfter", source = "balanceAfter")
    Transaction toDomain(TransactionResponse response);
}