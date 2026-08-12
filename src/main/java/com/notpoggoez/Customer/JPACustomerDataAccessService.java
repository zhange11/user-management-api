package com.notpoggoez.Customer;

import org.hibernate.sql.Update;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("jpa")
public class JPACustomerDataAccessService implements CustomerDao { // trade-offs between JPA and JDBC
    private final CustomerRepository customerRepository;
    public JPACustomerDataAccessService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> selectAllCustomers() {
        Page<Customer> page = customerRepository.findAll(Pageable.ofSize(1000));
        return page.getContent();
    }

    @Override
    public Optional<Customer> selectCustomerByID(Integer id) {
        return customerRepository.findById(id);
    }

    @Override
    public void insertCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    @Override
    public boolean existsCustomerByEmail(String email) {
        return customerRepository.existsCustomerByEmail(email);
    }

    public boolean existsCustomerByID(Integer ID) {
        return customerRepository.existsCustomerByID(ID);
    }

    @Override
    public void deleteCustomerByID(Integer ID) {
        customerRepository.deleteById(ID);
    }

    @Override
    public void updateCustomer(Customer update) {
        customerRepository.save(update);
    }

    @Override
    public Optional<Customer> selectCustomerByEmail(String email) {
        return customerRepository.findCustomerByEmail(email);
    }

    @Override
    public void updateCustomerProfileImageId(String profileImageID, Integer customerID) {
        customerRepository.updateCustomerProfileImageId(profileImageID, customerID);
    }

}
