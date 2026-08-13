package com.notpoggoez.Customer;

public record CustomerUpdateRequest(
        String username,
        String email,
        Integer age
) {
}
