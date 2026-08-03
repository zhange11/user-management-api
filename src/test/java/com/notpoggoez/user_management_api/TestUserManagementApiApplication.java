package com.notpoggoez.user_management_api;

import com.notpoggoez.UserManagementApiApplication;
import org.springframework.boot.SpringApplication;

public class TestUserManagementApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(UserManagementApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
