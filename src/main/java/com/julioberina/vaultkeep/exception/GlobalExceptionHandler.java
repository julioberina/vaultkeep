package com.julioberina.vaultkeep.exception;

import com.julioberina.vaultkeep.payload.response.ErrorMessage;
import com.julioberina.vaultkeep.payload.response.ValidationErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();

		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		ValidationErrorResponse errorResponse = new ValidationErrorResponse(
			HttpStatus.BAD_REQUEST.value(),
			LocalDateTime.now(),
			"Validation Failed!",
			errors
		);

		return ResponseEntity.badRequest().body(errorResponse);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorMessage> handleGlobalException(Exception ex, WebRequest request) {
		logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);

		ErrorMessage errorMessage = new ErrorMessage(
			HttpStatus.INTERNAL_SERVER_ERROR.value(),
			LocalDateTime.now(),
			"An internal server error occurred!",
			request.getDescription(false)
		);

		return ResponseEntity.internalServerError().body(errorMessage);
	}
}
