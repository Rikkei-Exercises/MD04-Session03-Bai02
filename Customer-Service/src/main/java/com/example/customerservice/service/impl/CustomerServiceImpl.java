package com.example.customerservice.service.impl;

import com.example.customerservice.dto.request.CustomerRequestDTO;
import com.example.customerservice.dto.response.CustomerResponseDTO;
import com.example.customerservice.entity.Customer;
import com.example.customerservice.exception.InvalidCredentialsException;
import com.example.customerservice.exception.ResourceNotFoundException;
import com.example.customerservice.repository.CustomerRepository;
import com.example.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CustomerResponseDTO register(CustomerRequestDTO request) {
        String fullName = Optional.ofNullable(request.getFullName())
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("fullName is required"));

        String email = normalizeEmail(request.getEmail());
        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        Customer customer = Customer.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        return toResponse(savedCustomer);
    }

    @Override
    public CustomerResponseDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng với ID " + id + " không tồn tại!"));
        return toResponse(customer);
    }

    @Override
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng với ID " + id + " không tồn tại!"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            customer.setFullName(request.getFullName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = normalizeEmail(request.getEmail());
            if (!newEmail.equals(customer.getEmail()) && customerRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email already exists");
            }
            customer.setEmail(newEmail);
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            customer.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return toResponse(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng với ID " + id + " không tồn tại!"));
        customerRepository.delete(customer);
    }

    @Override
    public CustomerResponseDTO login(CustomerRequestDTO request) {
        String email = normalizeEmail(request.getEmail());
        String password = request.getPassword();

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("email or password incorrect "));

        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw new InvalidCredentialsException("email or password incorrect ");
        }

        return toResponse(customer);
    }

    private String normalizeEmail(String email) {
        return Optional.ofNullable(email)
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException("email is required"));
    }

    private CustomerResponseDTO toResponse(Customer customer) {
        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .build();
    }
}
