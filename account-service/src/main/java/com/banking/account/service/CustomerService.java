package com.banking.account.service;

import com.banking.account.dto.CustomerRequest;
import com.banking.account.dto.CustomerResponse;
import com.banking.account.entity.Customer;
import com.banking.account.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        Optional<Customer> existing = customerRepository.findByEmail(request.getEmail());
        if (existing.isPresent()) {
            Customer c = existing.get();
            log.warn("Create customer rejected: requested email '{}' already exists in DB as customer id={} name='{}' email='{}'",
                    request.getEmail(), c.getId(), c.getName(), c.getEmail());
            throw new IllegalArgumentException("Customer with email already exists: " + request.getEmail());
        }
        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        customer = customerRepository.save(customer);
        return CustomerResponse.from(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::from)
                .collect(Collectors.toList());
    }
}
