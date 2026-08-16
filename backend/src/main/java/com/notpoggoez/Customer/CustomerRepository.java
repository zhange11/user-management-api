package com.notpoggoez.Customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findCustomerByEmail(String email);
    boolean existsCustomerByEmail(String email);
    boolean existsCustomerByID(Integer id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Customer c SET c.profileImageId =?1 WHERE c.ID = ?2")
    void updateCustomerProfileImageId(String profileImageId, Integer customerID);
}
