package com.notpoggoez.Customer;

import com.notpoggoez.Exception.ResourceNotFoundException;
import com.notpoggoez.Exception.DuplicateResourceException;
import com.notpoggoez.Exception.RequestValidationException;
import com.notpoggoez.S3.S3Buckets;
import com.notpoggoez.S3.S3Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
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
                customerRegistrationRequest.age(),
                customerRegistrationRequest.gender(),
                passwordEncoder.encode(customerRegistrationRequest.password()));

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

    public void updateCustomer(Integer customerId, CustomerUpdateRequest updateRequest) {
        Customer customer = customerDao.selectCustomerByID(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "customer with id [%s] not found".formatted(customerId)
                ));

        boolean changes = false;

        if (updateRequest.username() != null && !updateRequest.username().equals(customer.getUsername())) {
            customer.setUsername(updateRequest.username());
            changes = true;
        }

        if (updateRequest.age() != null && !updateRequest.age().equals(customer.getAge())) {
            customer.setAge(updateRequest.age());
            changes = true;
        }

        if (updateRequest.email() != null && !updateRequest.email().equals(customer.getEmail())) {
            if (customerDao.existsCustomerByEmail(updateRequest.email())) {
                throw new DuplicateResourceException(
                        "email already taken"
                );
            }
            customer.setEmail(updateRequest.email());
            changes = true;
        }

        if (!changes) {
            throw new RequestValidationException("no data changes found");
        }

        customerDao.updateCustomer(customer);
    }

    public void uploadCustomerProfileImage(Integer customerId,
                                           MultipartFile file) {
        existsCustomer(customerId);
        String profileImageId = UUID.randomUUID().toString();
        try {
            s3service.putObject(
                    s3Buckets.getCustomer(),
                    "profile-images/%s/%s".formatted(customerId, profileImageId),
                    file.getBytes()
            );
        } catch (IOException e) {
            throw new RuntimeException("failed to upload profile image", e);
        }
        customerDao.updateCustomerProfileImageId(profileImageId, customerId);
    }


    public byte[] getCustomerProfileImage(Integer customerId) {
        var customer = customerDao.selectCustomerByID(customerId)
                .map(customerDTOMapping)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "customer with id [%s] not found".formatted(customerId)
                ));

        if (StringUtils.isBlank(customer.profileImageID())) {
            throw new ResourceNotFoundException(
                    "customer with id [%s] profile image not found".formatted(customerId));
        }

        byte[] profileImage = s3service.getObject(
                s3Buckets.getCustomer(),
                "profile-images/%s/%s".formatted(customerId, customer.profileImageID())
        );
        return profileImage;
    }



}
