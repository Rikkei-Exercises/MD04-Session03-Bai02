package com.example.customerservice.service;

import com.example.customerservice.dto.request.CustomerRequestDTO;
import com.example.customerservice.dto.response.CustomerResponseDTO;

public interface CustomerService {

    CustomerResponseDTO register(CustomerRequestDTO request);

    CustomerResponseDTO getCustomerById(Long id);

    CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO request);

    void deleteCustomer(Long id);

    CustomerResponseDTO login(CustomerRequestDTO request);
}
