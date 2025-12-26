package com.julioberina.vaultkeep.payload.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ValidationErrorResponse(
	int statusCode,
	LocalDateTime timestamp,
	String message,
	Map<String, String> errors
) {}
