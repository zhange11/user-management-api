package com.notpoggoez.Customer;

import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.notpoggoez.Exception.DuplicateResourceException;
import com.notpoggoez.S3.S3Buckets;
import com.notpoggoez.S3.S3Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    private final S3Buckets s3Buckets;
    private final S3Service s3service;
    private final CustomerDTOMapping customerDTOMapping;
    private final CustomerDao customerDao;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(@Qualifier("jpa") CustomerDao customerDao, S3Buckets s3buckets, S3Service s3service, CustomerDTOMapping customerDTOMapping, PasswordEncoder passwordEncoder) {
        this.customerDao = customerDao;
        this.s3Buckets = s3buckets;
        this.s3service = s3service;
        this.customerDTOMapping = customerDTOMapping;
        this.passwordEncoder = passwordEncoder;
    }

    public List<CustomerDTO> getAllCustomers() {
        return customerDao.selectAllCustomers()
                .stream()
                .map(customerDTOMapping)
                .collect(Collectors.toList());
    }

    public CustomerDTO getCustomer(Integer id) {
        return customerDao.selectCustomerByID(id)
                .map(customerDTOMapping)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "customer with id [%s] not found".formatted(id)
                ));
    }

    public void addCustomer(RegistrationRequest customerRegistrationRequest) {
        String email = customerRegistrationRequest.email();
        if (customerDao.existsCustomerByEmail(email)) {
            throw new DuplicateResourceException(
                    "email already taken"
            );
        }

        Customer customer = new Customer(
                customerRegistrationRequest.username(),
                customerRegistrationRequest.email(),
                passwordEncoder.encode(customerRegistrationRequest.password()),
                customerRegistrationRequest.age(),
                customerRegistrationRequest.gender();

        customerDao.insertCustomer(customer);
    }

    private void existsCustomer(Integer customerId) {
        if (!customerDao.existsCustomerByID(customerId)) {
            throw new ResourceNotFoundException(
                    "customer with id [%s] not found".formatted(customerId)
            );
        }
    }

    public void deleteCustomerById(Integer customerId) {
        existsCustomer(customerId);
        customerDao.deleteCustomerByID(customerId);
    }




}
