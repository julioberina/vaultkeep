package com.julioberina.vaultkeep.payload.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RegisterRequest(
	@NotBlank(message = "Username is required")
	@Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Username must be alphanumeric")
	String username,

	@NotBlank(message = "Password is required")
	@Size(min = 8, max = 40, message = "Password must be between 8 and 40 characters")
	String password
) {}
