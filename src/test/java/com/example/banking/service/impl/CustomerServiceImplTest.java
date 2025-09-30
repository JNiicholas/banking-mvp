package com.example.banking.service.impl;

import com.example.banking.dto.CreateCustomerRequest;
import com.example.banking.entity.CustomerEntity;
import com.example.banking.exception.NotFoundException;
import com.example.banking.keycloak.service.KeycloakProvisioningService;
import com.example.banking.mapper.CustomerEntityMapper;
import com.example.banking.model.Customer;
import com.example.banking.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private CustomerEntityMapper customerEntityMapper;
    @Mock private KeycloakProvisioningService keycloakProvisioningService;

    @InjectMocks private CustomerServiceImpl customerService; // class under test

    @Test
    void createCustomer_ok() {
        // given
        var req = CreateCustomerRequest.builder()
                .firstName("Jonas")
                .lastName("Iqbal")
                .email("jonas@iqbal.dk")
                .build();

        var savedId = UUID.randomUUID();

        var kcUserId = UUID.randomUUID().toString();
        given(keycloakProvisioningService.createUser(any())).willReturn(kcUserId);
        willDoNothing().given(keycloakProvisioningService).setUserPassword(eq(kcUserId), anyString(), anyBoolean());

        var savedEntity = CustomerEntity.builder()
                .id(savedId)
                .firstName(req.firstName())
                .lastName(req.lastName())
                .email(req.email())
                .externalAuthId(UUID.fromString(kcUserId))
                .build();
        var domain = Customer.builder()
                .id(savedId)
                .firstName(req.firstName())
                .lastName(req.lastName())
                .email(req.email())
                .externalAuthId(UUID.fromString(kcUserId))
                .build();

        // repo will return a persisted entity (id filled by DB)
        given(customerRepository.save(any(CustomerEntity.class))).willReturn(savedEntity);
        // mapper maps entity -> domain
        given(customerEntityMapper.toDomain(savedEntity)).willReturn(domain);

        // when
        var result = customerService.createCustomer(req);

        // then
        assertNotNull(result);
        assertEquals(savedId, result.getId());
        assertEquals("Jonas", result.getFirstName());
        assertEquals("Iqbal", result.getLastName());
        assertEquals("jonas@iqbal.dk", result.getEmail());

        // verify interactions
        then(customerRepository).should(times(1)).save(any(CustomerEntity.class));
        then(customerEntityMapper).should(times(1)).toDomain(savedEntity);
        then(keycloakProvisioningService).should().createUser(any());
        then(keycloakProvisioningService).should().setUserPassword(eq(kcUserId), anyString(), anyBoolean());
        then(customerRepository).shouldHaveNoMoreInteractions();
        then(customerEntityMapper).shouldHaveNoMoreInteractions();
    }

    @Test
    void getCustomer_found() {
        // given
        var id = UUID.randomUUID();
        var entity = CustomerEntity.builder()
                .id(id).firstName("Alice").lastName("Smith").email("alice@example.com").build();
        var domain = Customer.builder()
                .id(id).firstName("Alice").lastName("Smith").email("alice@example.com").build();

        given(customerRepository.findOneById(id)).willReturn(Optional.of(entity));
        given(customerEntityMapper.toDomain(entity)).willReturn(domain);

        // when
        var result = customerService.getCustomer(id);

        // then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Alice", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("alice@example.com", result.getEmail());

        then(customerRepository).should().findOneById(id);
        then(customerEntityMapper).should().toDomain(entity);
        then(customerRepository).shouldHaveNoMoreInteractions();
        then(customerEntityMapper).shouldHaveNoMoreInteractions();
    }

    @Test
    void getCustomer_notFound() {
        // given
        var id = UUID.randomUUID();
        given(customerRepository.findOneById(id)).willReturn(Optional.empty());

        // when / then
        assertThrows(NotFoundException.class, () -> customerService.getCustomer(id));

        then(customerRepository).should().findOneById(id);
        then(customerEntityMapper).shouldHaveNoInteractions();
    }

    @Test
    void getAllCustomers_ok() {

        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();

        var e1 = CustomerEntity.builder().id(id1).firstName("A").lastName("One").email("a@example.com").build();
        var e2 = CustomerEntity.builder().id(id2).firstName("B").lastName("Two").email("b@example.com").build();
        var d1 = Customer.builder().id(id1).firstName("A").lastName("One").email("a@example.com").build();
        var d2 = Customer.builder().id(id2).firstName("B").lastName("Two").email("b@example.com").build();

        given(customerRepository.findAllWithAccounts()).willReturn(List.of(e1, e2));
        given(customerEntityMapper.toDomain(e1)).willReturn(d1);
        given(customerEntityMapper.toDomain(e2)).willReturn(d2);

        var result = customerService.getAllCustomers();

        assertEquals(2, result.size());
        assertEquals(id1, result.get(0).getId());
        assertEquals(id2, result.get(1).getId());

        then(customerRepository).should().findAllWithAccounts();
        then(customerEntityMapper).should().toDomain(e1);
        then(customerEntityMapper).should().toDomain(e2);
        then(customerRepository).shouldHaveNoMoreInteractions();
        then(customerEntityMapper).shouldHaveNoMoreInteractions();
    }
}