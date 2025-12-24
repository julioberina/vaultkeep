package com.julioberina.vaultkeep.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorMessage {
	private int statusCode;
	private LocalDateTime timestamp;
	private String message;
	private String description;
}
