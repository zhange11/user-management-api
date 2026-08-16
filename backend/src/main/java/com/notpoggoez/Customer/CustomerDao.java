package com.notpoggoez.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerDao {
    List<Customer> selectAllCustomers();
    Optional<Customer> selectCustomerByID(Integer ID);
    void insertCustomer(Customer customer);
    boolean existsCustomerByEmail(String email);
    boolean existsCustomerByID(Integer ID);
    void deleteCustomerByID(Integer ID);
    void updateCustomer(Customer update);
    Optional<Customer> selectCustomerByEmail(String email);
    void updateCustomerProfileImageId(String profileImageID, Integer customerID);
}
