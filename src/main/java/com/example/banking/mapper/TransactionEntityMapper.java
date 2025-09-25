package com.example.banking.mapper;

import com.example.banking.entity.TransactionEntity;
import com.example.banking.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionEntityMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "timestamp", source = "timestamp")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "balanceAfter", source = "balanceAfter")
    Transaction toDomain(TransactionEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "timestamp", source = "timestamp")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "balanceAfter", source = "balanceAfter")
    TransactionEntity toEntity(Transaction domain);
}