package com.example.banking.mapper;

import com.example.banking.entity.CustomerEntity;
import com.example.banking.model.Customer;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerEntityMapperTest {

    private final CustomerEntityMapper mapper = Mappers.getMapper(CustomerEntityMapper.class);

    @Test
    void toDomain_mapsAllFields() {
        UUID id = UUID.randomUUID();
        var entity = CustomerEntity.builder()
                .id(id)
                .name("Alice")
                .email("alice@example.com")
                .build();

        Customer domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(id, domain.getId());
        assertEquals("Alice", domain.getName());
        assertEquals("alice@example.com", domain.getEmail());
    }

    @Test
    void toEntity_mapsAllFields() {
        UUID id = UUID.randomUUID();
        var domain = Customer.builder()
                .id(id)
                .name("Bob")
                .email("bob@example.com")
                .build();

        CustomerEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertEquals("Bob", entity.getName());
        assertEquals("bob@example.com", entity.getEmail());
    }
}