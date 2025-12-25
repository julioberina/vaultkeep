package com.julioberina.vaultkeep.payload.response;

import java.time.LocalDateTime;

public record ErrorMessage(int statusCode, LocalDateTime timestamp, String message, String description) {}
