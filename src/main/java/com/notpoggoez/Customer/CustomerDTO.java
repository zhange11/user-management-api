package com.notpoggoez.Customer;

import java.util.List;

public record CustomerDTO(
        Integer ID,
        String username,
        String gender,
        String email,
        Integer age,
        String profileImageID,
        List<String> role
) {

}
