package com.example.banking.mapper;

import com.example.banking.entity.CustomerEntity;
import java.util.UUID;
import com.example.banking.dto.CustomerResponse;
import com.example.banking.model.Customer;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.junit.jupiter.api.Assertions.*;

class CustomerMapperTest {

    private final CustomerMapper mapper = Mappers.getMapper(CustomerMapper.class);

    @Test
    void toResponse_mapsAllFields() {
        UUID id = UUID.randomUUID();
        var domain = Customer.builder()
                .id(id)
                .name("Alice")
                .email("alice@example.com")
                .build();

        CustomerResponse dto = mapper.toResponse(domain);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals("Alice", dto.name());
        assertEquals("alice@example.com", dto.email());
    }

    @Test
    void toDomain_mapsAllFields() {
        UUID id = UUID.randomUUID();
        var entity = CustomerEntity.builder()
                .id(id)
                .name("Alice")
                .email("alice@example.com")
                .build();

        var domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(id, domain.getId());
        assertEquals("Alice", domain.getName());
        assertEquals("alice@example.com", domain.getEmail());
    }
}