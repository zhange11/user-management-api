package com.notpoggoez.Security;

import com.notpoggoez.Customer.CustomerRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {
    private final CustomerRepository customerRepository;
    public CustomUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return customerRepository.findCustomerByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }


}
