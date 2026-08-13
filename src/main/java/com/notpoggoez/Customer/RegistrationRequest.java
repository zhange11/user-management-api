package com.notpoggoez.Customer;

public record RegistrationRequest(
        String username,
        String email,
        String password,
        Integer age,
        Gender gender
) {
}
